package rwalerow.services

import java.sql.Timestamp
import java.time.LocalDateTime

import rwalerow.domain._
import rwalerow.rest.CreateDiscussion
import rwalerow.utils.{Configuration, PersistenceModule}

import scala.concurrent.Future

class DiscussionRestLogicService(modules: Configuration with PersistenceModule) {

  def createDiscussion(createDiscussion: CreateDiscussion): Future[Secret] = {
    val discussion = Discussion(subject = Subject(createDiscussion.subject))
    val post = Post(
      nick = Nick(createDiscussion.nick),
      contents = Contents(createDiscussion.contents),
      email = Email(createDiscussion.email),
      secret = Secret(Secret.generate),
      createDate = Timestamp.valueOf(LocalDateTime.now()),
      discussionId = 0L
    )

    modules.discussionQueries.createDiscussion(discussion, post)
  }

  def listDiscussionByPostDates(limit: Option[Int] = None, offset: Option[Int] = None): Future[Seq[Discussion]] = {

    val maxLimit         = modules.config.getInt("limit.discussions")
    val calculatedLimit  = limit.filter(_ < maxLimit).getOrElse(maxLimit)
    val calculatedOffset = offset.getOrElse(0)

    modules.discussionQueries.listDiscussionByPostDates(calculatedLimit, calculatedOffset)
  }
}
