package rwalerow.utils

import rwalerow.services.{DiscussionQueriesExtended, DiscussionRestLogicService, PostQueriesExtended}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

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
  val discussionService: DiscussionRestLogicService
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule with RestLogicServices {
  this: Configuration =>

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("postgres")
  override implicit val profile: JdbcProfile = dbConfig.driver
  override implicit val db: JdbcBackend#DatabaseDef = dbConfig.db

  override val postQueries = new PostQueriesExtended
  override val discussionQueries = new DiscussionQueriesExtended(postQueries)

  override val discussionService: DiscussionRestLogicService = new DiscussionRestLogicService(this)
}


