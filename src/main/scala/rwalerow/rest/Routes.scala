package rwalerow.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import rwalerow.utils.{Configuration, PersistenceModule}
import rwalerow.domain.JsonProtocol._

import scala.util.{Failure, Success}

class Routes(modules: Configuration with PersistenceModule) extends Directives {

  def initialRoute = pathPrefix("start") {
    onComplete(modules.postDao.list) {
      case Success(seq) => complete(seq)
      case Failure(x) => complete(StatusCodes.InternalServerError, s"error occures ${x.getMessage}")
    }
  }

  val routes = initialRoute
}
