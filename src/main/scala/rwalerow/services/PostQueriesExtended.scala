package rwalerow.services

import rwalerow.domain.{Contents, Post, Posts, Secret}
import rwalerow.utils.BaseDaoImpl
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.Future


class PostQueriesExtended(implicit override val db: JdbcProfile#Backend#Database, implicit override val profile: JdbcProfile)
  extends BaseDaoImpl[Posts, Post](TableQuery[Posts]) {
  def updateBySecret(secret: Secret, contents: Contents): Future[Int] = db.run {
    val content = for {p <- tableQuery if p.secret === secret} yield p.contents
    content.update(contents)
  }
}
