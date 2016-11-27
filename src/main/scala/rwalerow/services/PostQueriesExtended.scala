package rwalerow.services

import java.sql.Timestamp

import rwalerow.domain.{Contents, Post, Posts, Secret}
import rwalerow.utils.BaseDaoImpl
import slick.driver.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.Future


class PostQueriesExtended(implicit override val db: JdbcProfile#Backend#Database, implicit override val profile: JdbcProfile)
  extends BaseDaoImpl[Posts, Post](TableQuery[Posts]) {

  import profile.api._

  def updateBySecret(discussionId: Long, secret: Secret, contents: Contents): Future[Int] = db.run {
    val content = for {
      p <- tableQuery if p.secret === secret && p.discussionId === discussionId
    } yield p.contents
    content.update(contents)
  }

  def countBy(discusisonId: Long, f: Posts => Rep[Boolean]): Future[Int] = db.run {
    tableQuery
      .filter(_.discussionId === discusisonId)
      .filter(f)
      .length.result
  }

  def countBefore(discussionId: Long, date: Timestamp): Future[Int] = countBy(discussionId, _.createDate < date)
  def countAfter(discussionId: Long, date: Timestamp): Future[Int] = countBy(discussionId, _.createDate > date)

  def postWithIndex(discussionId:Long, id: Long): Future[Option[(Post, Long)]] = db.run {
    tableQuery
      .filter(_.discussionId === discussionId)
      .sortBy(_.createDate)
      .zipWithIndex
      .filter{ case (p, i) => p.id === id }
      .result.headOption
  }

  def findInRange(takeBefore: Int, takeAfter: Int, postIndex: Long = 0, discussionId: Long): Future[Seq[Post]] = db.run {
    tableQuery
      .filter(_.discussionId === discussionId)
      .sortBy(_.createDate)
      .zipWithIndex
      .filter{
        case (p, i) => i >= postIndex - takeBefore && i <= postIndex + takeAfter
      }.map(_._1).result
  }
}
