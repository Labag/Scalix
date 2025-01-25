package scalix.services

import org.json4s.*
import org.json4s.jackson.JsonMethods.*

import java.io.File
import scala.collection.mutable

object ScalixService {
  implicit val formats: Formats = DefaultFormats
  val key = "87037d6d53baf2c98bb355aae0343d1f"
  private val ActorCache: mutable.Map[(String, String), Int] = mutable.Map.empty
  private val ActorMoviesCache: mutable.Map[Int, Set[(Int, String)]] = mutable.Map.empty
  private val MovieDirectorCache: mutable.Map[Int, Set[(Int, String)]] = mutable.Map.empty
  private val CollaborationsCache: mutable.Map[(FullName, FullName), Set[(String, String)]] = mutable.Map.empty

  private case class Person(id: Int, name: String, known_for_department: String)

  private case class Movie(id: Int, title: String)

  case class FullName(firstName: String, lastName: String)

  def findActorId(name: String, surname: String): Option[Int] =
    if (ActorCache.contains((name, surname))) {
      Some(ActorCache((name, surname)))
    } else {
      val url = s"https://api.themoviedb.org/3/search/person?query=$name%20$surname&api_key=$key"
      val source = scala.io.Source.fromURL(url)
      try {
        val contents = source.mkString
        val json = parse(contents)
        val results = json \ "results"
        ActorCache((name, surname)) = results.extract[List[Person]].head.id
        results.extract[List[Person]].headOption.map(_.id)
      } finally {
        source.close()
      }
    }

  def findActorMovies(actorId: Int): Set[(Int, String)] = {
    val url = s"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=$key"
    val path = s"./data/actor$actorId.json"
    if (ActorMoviesCache.contains(actorId)) {
      ActorMoviesCache(actorId)
    } else {
      val contents: String = getFromCache(url, path)
      val json = parse(contents)
      val results = json \ "cast"
      ActorMoviesCache(actorId) = results.extract[List[Movie]].map(m => (m.id, m.title)).toSet
      results.extract[List[Movie]].map(m => (m.id, m.title)).toSet
    }
  }

  def findMovieDirector(movieId: Int): Option[(Int, String)] =
    val url = s"https://api.themoviedb.org/3/movie/$movieId/credits?api_key=$key"
    val path = s"./data/movie$movieId.json"

    if (MovieDirectorCache.contains(movieId)) {
      MovieDirectorCache(movieId).headOption
    } else {
      val contents: String = getFromCache(url, path)
      val json = parse(contents)
      val results = json \ "crew"
      val director = results.extract[List[Person]].find(_.known_for_department == "Directing").map(p => (p.id, p.name))
      director.foreach(d => MovieDirectorCache(movieId) = Set(d))
      director
    }

  def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] =
    val fullNameActor1 = FullName(actor1.firstName, actor1.lastName)
    val fullNameActor2 = FullName(actor2.firstName, actor2.lastName)

    if (CollaborationsCache.contains(fullNameActor1, fullNameActor2)) {
      CollaborationsCache((fullNameActor1, fullNameActor2))
    } else {
      val actor1Id = findActorId(actor1.firstName, actor1.lastName)
      val actor2Id = findActorId(actor2.firstName, actor2.lastName)
      val actor1Movies = actor1Id.map(findActorMovies).getOrElse(Set.empty)
      val actor2Movies = actor2Id.map(findActorMovies).getOrElse(Set.empty)
      val commonMovies = actor1Movies.intersect(actor2Movies)
      val result = commonMovies.flatMap { case (movieId, title) =>
        findMovieDirector(movieId).map { case (_, director) =>
          (director, title)
        }
      }
      CollaborationsCache((fullNameActor1, fullNameActor2)) = result
      result
    }

  def mostFrequentCollaborators(): Set[(FullName, FullName)] = {
    val actors = ActorCache.values.toSet

    val pairs = for {
      actor1 <- actors
      actor2 <- actors
      if actor1 < actor2
    } yield (actor1, actor2)

    val collaborations = pairs.flatMap { case (actor1, actor2) =>
      for {
        name1 <- ActorCache.find { case (_, id) => id == actor1 }.map(_._1)
        name2 <- ActorCache.find { case (_, id) => id == actor2 }.map(_._1)
      } yield {
        val fullName1 = FullName(name1._1, name1._2)
        val fullName2 = FullName(name2._1, name2._2)
        (fullName1, fullName2) -> collaboration(fullName1, fullName2).size
      }
    }

    if (collaborations.isEmpty) {
      Set.empty[(FullName, FullName)]
    } else {
      val maxCollaborations = collaborations.maxBy(_._2)._2

      collaborations.collect {
        case (names, count) if count == maxCollaborations => names
      }
    }
  }

  private def getFromCache(url: String, path: String) = {
    val file = new File(path)
    val contents = if (file.exists()) {
      FileService.readFromFile(path).getOrElse {
        throw new RuntimeException(s"Error when reading the file $path")
      }
    } else {
      val fetchedContents = RequestService.fetchFromUrl(url).getOrElse {
        throw new RuntimeException(s"Error while fetching data from $url")
      }
      file.createNewFile()
      FileService.writeToFile(path, fetchedContents)
      fetchedContents
    }
    contents
  }
}
