package rwalerow.services

import org.scalatest.concurrent.ScalaFutures
import rwalerow.domain.Secret
import rwalerow.rest.{AbstractMockedConfigTest, CreateDiscussion}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DiscussionMockedConfigLogicServiceSpec extends AbstractMockedConfigTest with ScalaFutures {

  def actorRefFactory = system
  val modules = new Modules {}
  val discussionService = new DiscussionRestLogicService(modules)

  "Create discussion" should {
    "call create discussion query" in {
      (modules.discussionQueries.createDiscussion _).expects(*, *).returning(Future(Secret("secret")))
      val createDiscussion = CreateDiscussion("subject", "contents", "nick", "email@gmail.com")

      discussionService.createDiscussion(createDiscussion).futureValue shouldBe Secret("secret")
    }
  }

  "List discussions" should {
    "pass config limit to listDiscussions queries" in {
      (modules.discussionQueries.listDiscussionByPostDates _).expects(10, *) returns Future(List())
      (modules.conf.getInt _).expects(*).returning(10)

      discussionService.listDiscussionByPostDates(limit = Some(22))
    }
  }
}
