package rwalerow.rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}
import org.specs2.mock.Mockito
import rwalerow.domain.{Discussion, Discussions, Post, Posts}
import rwalerow.services.{DiscussionQueriesExtended, DiscussionRestLogicService, PostQueriesExtended, PostRestLogicService}
import rwalerow.utils.{BaseDao, ConfigurationImpl, PersistenceModule, RestLogicServices}

/**
  * source: https://github.com/cdiniz/slick-akka-http/blob/master/src/test/scala/rest/AbstractRestTest.scala
  */
trait AbstractRestTest extends WordSpec with Matchers with ScalatestRouteTest with Mockito {

  trait Modules extends ConfigurationImpl with PersistenceModule with RestLogicServices {
    val system = AbstractRestTest.this.system
    override val discussionQueries: DiscussionQueriesExtended = mock[DiscussionQueriesExtended]
    override val postQueries: PostQueriesExtended = mock[PostQueriesExtended]
    val conf = mock[Config]
    override val discussionLogicService = mock[DiscussionRestLogicService]
    override val postLogicService = mock[PostRestLogicService]
    override def config = conf
  }
}
