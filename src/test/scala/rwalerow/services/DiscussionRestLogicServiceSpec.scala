package rwalerow.services

import org.mockito.Mockito._
import org.scalatest.Matchers
import rwalerow.domain.{Discussion, Post, Secret}
import rwalerow.rest.{AbstractRestTest, CreateDiscussion}

import scala.concurrent.Future

class DiscussionRestLogicServiceSpec extends AbstractRestTest with Matchers {

  def actorRefFactory = system
  val modules = new Modules {}
  val disModules = new DiscussionRestLogicService(modules)

  "Create discussion" should {
    "call create discussion query" in {
      modules.discussionQueries.createDiscussion(any[Discussion], any[Post]) returns Future(Secret("secret"))
      val createDiscussion = CreateDiscussion("subject", "contents", "nick", "email@gmail.com")

      disModules.createDiscussion(createDiscussion)

      verify(modules.discussionQueries).createDiscussion(any[Discussion], any[Post])
    }
  }
}
