package rwalerow.services

import java.sql.Timestamp
import java.time.LocalDateTime

import rwalerow.domain._
import rwalerow.rest.CreateDiscussion
import rwalerow.utils.PersistenceModule

import scala.concurrent.Future

class DiscussionRestLogicService(persistence: PersistenceModule) {

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

    persistence.discussionQueries.createDiscussion(discussion, post)
  }
}
