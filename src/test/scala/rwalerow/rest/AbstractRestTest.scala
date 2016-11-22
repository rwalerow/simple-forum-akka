package rwalerow.rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}
import org.specs2.mock.Mockito
import rwalerow.domain.{Discussion, Discussions, Post, Posts}
import rwalerow.utils.{BaseDao, ConfigurationImpl, PersistenceModule}

/**
  * source: https://github.com/cdiniz/slick-akka-http/blob/master/src/test/scala/rest/AbstractRestTest.scala
  */
trait AbstractRestTest extends WordSpec with Matchers with ScalatestRouteTest with Mockito {

  trait Modules extends ConfigurationImpl with PersistenceModule {
    val system = AbstractRestTest.this.system
    override val discussionDao = mock[BaseDao[Discussions, Discussion]]
    override val postDao = mock[BaseDao[Posts, Post]]
    override def config = getConfig.withFallback(super.config)
  }

  def getConfig: Config = ConfigFactory.empty()
}
