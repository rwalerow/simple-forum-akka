package rwalerow.rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}
import rwalerow.domain.Discussions
import rwalerow.services.{DiscussionQueriesExtended, DiscussionRestLogicService, PostQueriesExtended, PostRestLogicService}
import rwalerow.utils.{Configuration, DbModule, PersistenceModule, RestLogicServices}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.lifted.TableQuery

class AbstractIntegrationTest extends WordSpec with Matchers with ScalatestRouteTest {

  trait Modules extends Configuration with PersistenceModule with DbModule with RestLogicServices {

    val configDefault = ConfigFactory.load("application-test.conf")
    private val dbConfig : DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("postgrestest", configDefault)
    override implicit val profile: JdbcProfile = dbConfig.driver
    override implicit val db: JdbcProfile#Backend#Database = dbConfig.db

    val system = AbstractIntegrationTest.this.system
    override val postQueries = new PostQueriesExtended(TableQuery[Discussions])
    override val discussionQueries = new DiscussionQueriesExtended(postQueries)

    override val discussionLogicService = new DiscussionRestLogicService(this)
    override val postLogicService = new PostRestLogicService(this)

    override def config = configDefault
  }
}
