package rwalerow.rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}
import rwalerow.services.{DiscussionQueriesExtended, PostQueriesExtended}
import rwalerow.utils.{Configuration, DbModule, PersistenceModule}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

class AbstractIntegrationTest extends WordSpec with Matchers with ScalatestRouteTest {

  trait Modules extends Configuration with PersistenceModule with DbModule {

    val configDefault = ConfigFactory.load("application-test.conf")
    private val dbConfig : DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("postgrestest", configDefault)
    override implicit val profile: JdbcProfile = dbConfig.driver
    override implicit val db: JdbcProfile#Backend#Database = dbConfig.db

    val system = AbstractIntegrationTest.this.system
    override val postQueries = new PostQueriesExtended
    override val discussionQueries = new DiscussionQueriesExtended(postQueries.tableQuery)

    override def config = configDefault
  }
}
