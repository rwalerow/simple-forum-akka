import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.server.Directives._

object FirstRoutes {
  val routes = {
    pathPrefix("hello") {
      complete("czesc Robert")
    }
  }
}

object FirstMicroservice extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  Http().bindAndHandle(FirstRoutes.routes, config.getString("http.interface"), config.getInt("http.port"))
}
