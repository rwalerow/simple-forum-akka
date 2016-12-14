package rwalerow.utils

import rwalerow.domain.Discussions
import rwalerow.services.{DiscussionQueriesExtended, DiscussionRestLogicService, PostQueriesExtended, PostRestLogicService}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import slick.lifted.TableQuery

/**
  * source: https://github.com/cdiniz/slick-akka-http/blob/master/src/main/scala/utils/PersistenceModule.scala
  */
trait Profile {
  val profile: JdbcProfile
}

trait DbModule extends Profile {
  val db: JdbcProfile#Backend#Database
}

trait PersistenceModule {
  val discussionQueries: DiscussionQueriesExtended
  val postQueries: PostQueriesExtended
}

trait RestLogicServices {
  val discussionLogicService: DiscussionRestLogicService
  val postLogicService: PostRestLogicService
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule with RestLogicServices {
  this: Configuration =>

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("postgres")
  override implicit val profile: JdbcProfile = dbConfig.driver
  override implicit val db: JdbcBackend#DatabaseDef = dbConfig.db

  override val postQueries = new PostQueriesExtended(TableQuery[Discussions])
  override val discussionQueries = new DiscussionQueriesExtended(postQueries)

  override val discussionLogicService = new DiscussionRestLogicService(this)
  override val postLogicService = new PostRestLogicService(this)
}


