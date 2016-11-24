package rwalerow.utils

import rwalerow.domain.{Discussion, Discussions, Post, Posts}
import rwalerow.services.PostQueriesExtended
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
  val discussionDao: BaseDao[Discussions, Discussion]
  val extendedPostQueries: PostQueriesExtended
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule {
  this: Configuration =>

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("postgres")
  override implicit val profile: JdbcProfile = dbConfig.driver
  override implicit val db: JdbcBackend#DatabaseDef = dbConfig.db

  override val discussionDao = new BaseDaoImpl[Discussions, Discussion](TableQuery[Discussions])
  override val extendedPostQueries = new PostQueriesExtended
}


