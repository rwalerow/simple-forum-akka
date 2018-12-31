import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import rwalerow.rest.Routes
import rwalerow.utils.{ConfigurationImpl, PersistenceModuleImpl}

object ForumMicroservice extends App {
  implicit val system       = ActorSystem()
  implicit val executor     = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val modules               = new ConfigurationImpl with PersistenceModuleImpl

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  Http().bindAndHandle(new Routes(modules).routes, config.getString("http.interface"), config.getInt("http.port"))
}
