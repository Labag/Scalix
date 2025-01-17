package scalix

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.DefaultFormats._
import java.io.PrintWriter

object Scalix extends App :
  implicit val formats: Formats = DefaultFormats
  val key = "87037d6d53baf2c98bb355aae0343d1f"

  private case class Person(id: Int, name: String, known_for_department: String)
  private case class Movie(id: Int, title: String)
  private case class FullName(firstName: String, lastName: String)

  println(findActorId("Tom", "Cruise"))
  println(findActorMovies(500))
  println(findMovieDirector(11873))
  println(collaboration(FullName("Tom", "Cruise"), FullName("Tom", "Hanks")))

  private def findActorId(name: String, surname: String): Option[Int] =
    val url = s"https://api.themoviedb.org/3/search/person?query=$name%20$surname&api_key=$key"
    val source = scala.io.Source.fromURL(url)
    val contents = source.mkString
    val json = parse(contents)
    val results = json \ "results"
    results.extract[List[Person]].headOption.map(_.id)

  private def findActorMovies(actorId: Int): Set[(Int, String)] =
    val url = s"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=$key"
    val source = scala.io.Source.fromURL(url)
    val contents = source.mkString
    val json = parse(contents)
    val results = json \ "cast"
    results.extract[List[Movie]].map(m => (m.id, m.title)).toSet

  private def findMovieDirector(movieId: Int): Option[(Int, String)] =
    val url = s"https://api.themoviedb.org/3/movie/$movieId/credits?api_key=$key"
    val source = scala.io.Source.fromURL(url)
    val contents = source.mkString
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