package rwalerow.repository

import java.sql.Timestamp

import rwalerow.domain._
import rwalerow.domain.common.BaseDaoImpl
import slick.driver.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class PostQueriesExtended(discussions: TableQuery[Discussions])(implicit override val db: JdbcProfile#Backend#Database, implicit override val profile: JdbcProfile)
  extends BaseDaoImpl[Posts, Post](TableQuery[Posts]) {

  import profile.api._

  def updateBySecret(discussionId: Long, secret: Secret, contents: Contents): Future[Int] = db.run {
    val content = for {
      p <- tableQuery if p.secret === secret && p.discussionId === discussionId
    } yield p.contents
    content.update(contents)
  }

  def countBy(discussionId: Long, f: Posts => Rep[Boolean]): Future[Int] = db.run(countByQ(discussionId, f).result)
  def countBefore(discussionId: Long, date: Timestamp): Future[Int] = db.run(countPostsBeforeDateC(discussionId, date).result)
  def countAfter(discussionId: Long, date: Timestamp): Future[Int] = db.run(countPostsAfterDateC(discussionId, date).result)

  private def countByQ(discussionId: Rep[Long], f: Posts => Rep[Boolean]): Rep[Int] =
    tableQuery
      .filter(_.discussionId === discussionId)
      .filter(f)
      .length

  private def countBeforeR(discussionId: Rep[Long], date: Rep[Timestamp]): Rep[Int] = countByQ(discussionId, _.createDate < date)
  private def countAfterR(discussionId: Rep[Long], date: Rep[Timestamp]): Rep[Int] = countByQ(discussionId, _.createDate > date)
  private def countPostsBeforeDateC = Compiled(countBeforeR _)
  private def countPostsAfterDateC = Compiled(countAfterR _)

  def postWithIndex(discussionId: Long, id: Long): Future[Option[(Post, Long)]] =
    db.run(findPostWithIndexC(discussionId, id).result.headOption)

  private def postWithIndexR(discussionId: Rep[Long], id: Rep[Long]) =
    tableQuery
      .filter(_.discussionId === discussionId)
      .sortBy(_.createDate)
      .zipWithIndex
      .filter{ case (p, i) => p.id === id }
      .take(1)

  private def findPostWithIndexC = Compiled(postWithIndexR _)

  def findInRange(takeBefore: Int, takeAfter: Int, postIndex: Long = 0, discussionId: Long): Future[Seq[Post]] = db.run {
    tableQuery
      .filter(_.discussionId === discussionId)
      .sortBy(_.createDate)
      .zipWithIndex
      .filter{
        case (p, i) => i >= postIndex - takeBefore && i <= postIndex + takeAfter
      }.map(_._1).result
  }

  def createPost(post: Post): Future[Secret] = db.run {
   (for {
      exists <- discussions.filter(_.id === post.discussionId).result.headOption if exists.isDefined
      createPostId <- insertQ(post)
    } yield post.secret).transactionally
  }

  def listPostsWithLimits(drop: Int, limit: Int): Future[Seq[Post]] = db.run(listPostsWithLimitsC(drop, limit).result)
  private def listPostsWithLimitR(drop: ConstColumn[Long], limit: ConstColumn[Long]) = tableQuery.drop(drop).take(limit)
  private def listPostsWithLimitsC = Compiled(listPostsWithLimitR _)

  def deletePost(discussionId: Long, secret: Secret): Future[Int] = deleteByFilter(x => x.discussionId === discussionId && x.secret === secret)
}
