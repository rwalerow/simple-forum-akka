package rwalerow.utils

import rwalerow.domain.{Discussion, Discussions, Post, Posts}
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
  val postDao: BaseDao[Posts, Post]
  val discussionDao: BaseDao[Discussions, Discussion]
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule {
  this: Configuration =>

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("postgres")
  override implicit val profile: JdbcProfile = dbConfig.driver
  override implicit val db: JdbcBackend#DatabaseDef = dbConfig.db

  override val postDao = new BaseDaoImpl[Posts, Post](TableQuery[Posts])
  override val discussionDao = new BaseDaoImpl[Discussions, Discussion](TableQuery[Discussions])
}


