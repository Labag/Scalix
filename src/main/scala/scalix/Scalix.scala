package scalix

import scalix.services.ScalixService.*

object Scalix extends App :
  println(findActorId("Leonardo", "Dicaprio"))
  println(findActorMovies(400))
  println(findMovieDirector(11873))
  println(collaboration(FullName("Tom", "Cruise"), FullName("Tom", "Hanks")))

  println(mostFrequentCollaborators())
