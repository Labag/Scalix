package scalix.services

import scala.io.Source
import scala.util.{Using, Try}

object RequestService {
  def fetchFromUrl(url: String): Option[String] = {
    Using(Source.fromURL(url)) { source =>
      source.mkString
    }.toOption
  }
}

