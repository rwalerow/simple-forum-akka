package rwalerow.services

import rwalerow.domain._
import rwalerow.utils.{BaseDBIODao, BaseDaoImpl, WithTableQuery}
import slick.driver.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DiscussionQueriesExtended(posts: BaseDBIODao[Posts, Post] with WithTableQuery[Posts, Post])
                               (implicit override val db: JdbcProfile#Backend#Database, implicit override val profile: JdbcProfile)
  extends BaseDaoImpl[Discussions, Discussion](TableQuery[Discussions]){

  import profile.api._

  def listDiscussionByPostDates(limit: Int = 50, offset: Int = 0): Future[Seq[Discussion]] = db.run {
    val newestPost = for {
          (disId, p) <- posts.tableQuery.groupBy{_.discussionId}
        } yield disId -> p.map(_.createDate).max

    val discussionWithDate = for {
      (dis, (disId, createDate)) <- tableQuery join newestPost on (_.id === _._1)
    } yield (dis, createDate)

    discussionWithDate
      .sortBy{ _._2.desc }
      .drop(offset)
      .take(limit)
      .map(_._1).result
  }

  def createDiscussion(discussion: Discussion, post: Post): Future[Secret] = db.run {
    (for {
      createdId <- insertQ(discussion)
      createdPostId <- posts.insertQ(post.copy(discussionId = createdId))
    } yield post.secret).transactionally
  }
}
