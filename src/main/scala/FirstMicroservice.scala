import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.server.Directives._
import rwalerow.rest.Routes
import rwalerow.utils.{ConfigurationImpl, PersistenceModuleImpl}

object FirstMicroservice extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val modules = new ConfigurationImpl with PersistenceModuleImpl

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  Http().bindAndHandle(new Routes(modules).routes, config.getString("http.interface"), config.getInt("http.port"))
}
