package scalix

import org.json4s.*
import org.json4s.jackson.JsonMethods.*
import org.json4s.DefaultFormats.*
import scalix.services.{FileService, RequestService}

import java.io.File
import scala.collection.mutable

object Scalix extends App :
  implicit val formats: Formats = DefaultFormats
  val key = "87037d6d53baf2c98bb355aae0343d1f"
  private val ActorCache: mutable.Map[(String, String), Int] = mutable.Map.empty

  private case class Person(id: Int, name: String, known_for_department: String)
  private case class Movie(id: Int, title: String)
  private case class FullName(firstName: String, lastName: String)

  //println(findActorId("Tom", "Cruise"))
  //println(findActorMovies(400))
  //println(findMovieDirector(5204))
  //println(collaboration(FullName("Tom", "Cruise"), FullName("Tom", "Hanks")))

  private def findActorId(name: String, surname: String): Option[Int] =
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

  private def findActorMovies(actorId: Int): Set[(Int, String)] = {
    val url = s"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=$key"
    val path = s"./data/actor$actorId.json"
    val contents: String = getFromCache(url, path)

    val json = parse(contents)
    val results = json \ "cast"
    results.extract[List[Movie]].map(m => (m.id, m.title)).toSet
  }

  private def getFromCache(url: String, path: String) = {
    val file = new File(path)

    val contents = if (file.exists()) {
      FileService.readFromFile(path).getOrElse {
        throw new RuntimeException(s"Erreur lors de la lecture du fichier $path")
      }
    } else {
      val fetchedContents = RequestService.fetchFromUrl(url).getOrElse {
        throw new RuntimeException(s"Erreur lors de la récupération des données")
      }
      file.createNewFile()
      FileService.writeToFile(path, fetchedContents)
      fetchedContents
    }
    contents
  }

  private def findMovieDirector(movieId: Int): Option[(Int, String)] =
    val url = s"https://api.themoviedb.org/3/movie/$movieId/credits?api_key=$key"
    val path = s"./data/movie$movieId.json"
    val contents: String = getFromCache(url, path)
    val json = parse(contents)
    val results = json \ "crew"
    results.extract[List[Person]].find(_.known_for_department == "Directing").map(p => (p.id, p.name))

  private def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] =
    val actor1Id = findActorId(actor1.firstName, actor1.lastName)
    val actor2Id = findActorId(actor2.firstName, actor2.lastName)
    val actor1Movies = actor1Id.map(findActorMovies).getOrElse(Set.empty)
    val actor2Movies = actor2Id.map(findActorMovies).getOrElse(Set.empty)
    val commonMovies = actor1Movies.intersect(actor2Movies)
    commonMovies.flatMap { case (movieId, title) =>
      findMovieDirector(movieId).map { case (_, director) =>
        (director, title)
      }
    }