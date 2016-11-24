package rwalerow.services

import rwalerow.domain.{Discussion, Discussions, Posts}
import rwalerow.utils.BaseDaoImpl
import slick.driver.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
  * Created by robert on 24.11.16.
  */
class DiscussionQueriesExtended(posts: TableQuery[Posts])
                               (implicit override val db: JdbcProfile#Backend#Database, implicit override val profile: JdbcProfile)
  extends BaseDaoImpl[Discussions, Discussion](TableQuery[Discussions]){

  import profile.api._

  def listDiscussionByPostDates(limit: Int = 50, offset: Int = 0): Future[Seq[Discussion]] = db.run {
    val maxPost =
          for {
          (disId, p) <- posts.groupBy{_.discussionId}
        } yield disId -> p.map(_.createDate).max

    val discussionWithDate = for {
      (dis, (disId, createDate)) <- tableQuery join maxPost on (_.id === _._1)
    } yield (dis, createDate)

    discussionWithDate
      .sortBy{ _._2.desc }
      .drop(offset)
      .take(limit)
      .map(_._1).result
  }
}
