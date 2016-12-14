package rwalerow.services

import org.mockito.Mockito._
import org.scalatest.Matchers
import org.specs2.matcher.AnyMatchers
import rwalerow.domain.{Discussion, Post, Secret}
import rwalerow.rest.{AbstractMockedConfigTest, CreateDiscussion}

import scala.concurrent.Future

class DiscussionMockedConfigLogicServiceSpec extends AbstractMockedConfigTest with Matchers with AnyMatchers {

  def actorRefFactory = system
  val modules = new Modules {}
  val discussionService = new DiscussionRestLogicService(modules)

  "Create discussion" should {
    "call create discussion query" in {
      modules.discussionQueries.createDiscussion(any[Discussion], any[Post]) returns Future(Secret("secret"))
      val createDiscussion = CreateDiscussion("subject", "contents", "nick", "email@gmail.com")

      discussionService.createDiscussion(createDiscussion)

      verify(modules.discussionQueries).createDiscussion(any[Discussion], any[Post])
    }
  }

  "List discussions" should {
    "pass config limit to listDiscussions queries" in {
      modules.discussionQueries.listDiscussionByPostDates(anyInt, anyInt) returns Future(List())
      modules.conf.getInt(anyString) returns 10

      discussionService.listDiscussionByPostDates(limit = Some(22))

      verify(modules.discussionQueries).listDiscussionByPostDates(limit = argThat(be_==(10)), offset = anyInt)
    }
  }
}
