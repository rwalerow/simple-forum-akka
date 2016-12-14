package rwalerow.rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.Config
import org.scalatest.{Matchers, WordSpec}
import org.specs2.mock.Mockito
import rwalerow.repository.{DiscussionQueriesExtended, PostQueriesExtended}
import rwalerow.services.{DiscussionRestLogicService, PostRestLogicService}
import rwalerow.utils.{ConfigurationImpl, PersistenceModule, RestLogicServices}

/**
  * source: https://github.com/cdiniz/slick-akka-http/blob/master/src/test/scala/rest/AbstractRestTest.scala
  */
trait AbstractMockedConfigTest extends WordSpec with Matchers with ScalatestRouteTest with Mockito {

  trait Modules extends ConfigurationImpl with PersistenceModule with RestLogicServices {
    val system = AbstractMockedConfigTest.this.system
    override val discussionQueries: DiscussionQueriesExtended = mock[DiscussionQueriesExtended]
    override val postQueries: PostQueriesExtended = mock[PostQueriesExtended]
    val conf = mock[Config]
    override val discussionLogicService = mock[DiscussionRestLogicService]
    override val postLogicService = mock[PostRestLogicService]
    override def config = conf
  }
}
