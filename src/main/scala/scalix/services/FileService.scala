package scalix.services

import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source
import scala.util.Using

object FileService {

  def writeToFile(path: String, content: String): Unit = {
    val file = new File(path)
     Using(new BufferedWriter(new FileWriter(file))) { writer =>
      writer.write(content)
    }.recover {
      case ex: Exception =>
        println(s"Erreur lors de l'écriture du fichier $path: ${ex.getMessage}")
    }
  }

  def readFromFile(path: String): Option[String] = {
    Using(Source.fromFile(path)) { source =>
      source.mkString
    }.toOption
  }
}
