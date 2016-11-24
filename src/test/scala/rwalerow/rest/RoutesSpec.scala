package rwalerow.rest

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.util.Helpers.Requiring
import org.mockito.ArgumentMatcher
import org.mockito.Mockito._
import org.scalatest.Matchers
import org.specs2.matcher.{AnyMatchers, BeEqualTo, NumericMatchers}
import rwalerow.domain.JsonProtocol._
import rwalerow.domain._

import scala.concurrent.Future
import slick.driver.PostgresDriver.api._

class RoutesSpec extends AbstractRestTest with Matchers with org.specs2.matcher.AnyMatchers{

  trait Mocks {
    def actorRefFactory = system
    val modules = new Modules {}
    val discussionRoutes = new Routes(modules)
  }

  "Discussion routes" should {

    "return empty array of discussions" in new Mocks {
      modules.discussionQueries.listDiscussionByPostDates(anyInt, anyInt) returns Future(List())
      modules.conf.getInt(anyString) returns 10

      Get("/discussions") ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        responseAs[List[Discussion]].isEmpty shouldEqual true
      }
    }

    "pass url limit to listDiscussions" in new Mocks {
      modules.discussionQueries.listDiscussionByPostDates(anyInt, anyInt) returns Future(List())
      modules.conf.getInt(anyString) returns 10

      Get("/discussions?limit=2") ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        verify(modules.discussionQueries).listDiscussionByPostDates(limit = argThat(be_==(2)), offset = anyInt)
      }
    }

    "pass config limit to listDiscussions" in new Mocks {
      modules.discussionQueries.listDiscussionByPostDates(anyInt, anyInt) returns Future(List())
      modules.conf.getInt(anyString) returns 10

      Get("/discussions?limit=22") ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        verify(modules.discussionQueries).listDiscussionByPostDates(limit = argThat(be_==(10)), offset = anyInt)
      }
    }

    "valid discussion be created" in new Mocks {
      modules.discussionQueries.insert(any[Discussion]) returns Future(1)
      modules.extendedPostQueries.insert(any[Post]) returns Future(1)

      Put("/discussion", CreateDiscussion("subject", "contents", "nick", "email@gmail.com")) ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
        verify(modules.extendedPostQueries).insert(any[Post])
        verify(modules.discussionQueries).insert(any[Discussion])
      }
    }

    "invalid discussion be filtered" in new Mocks {
      val invalidDiscussion = CreateDiscussion("subject", "contents", "thisisrealytolongnickforittobetrueandnoteasytoremember", "gmail.com")
      Put("/discussion", invalidDiscussion) ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        status shouldEqual BadRequest
        responseAs[List[String]].isEmpty shouldEqual false
        responseAs[List[String]] should contain theSameElementsAs List("Invalid address email format", "Nick is to long")
      }
    }
  }

  "Post routes" should {

    "create a valid post" in new Mocks {
      val validPost = CreatePost("contents", "nick", "email@gmail.com")
      val discussion = Discussion(Some(1), Subject("subject"))
      modules.extendedPostQueries.insert(any[Post]) returns Future(1)
      modules.discussionQueries.findById(anyLong) returns Future(Some(discussion))

      Put("/discussion/1/post", validPost) ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
        responseAs[String].length > 0 shouldEqual true
        /*
            Why it is called twice?
         */
//        verify(modules.postDao).insert(any[Post])
        verify(modules.discussionQueries).findById(anyLong)
      }
    }

    "delete valid post" in new Mocks {
      val secret = Secret("abcdefghijklmnoprstuwxyz")
      def f(x: Posts): Rep[Boolean] = x.id === 1L && x.secret === secret
      modules.extendedPostQueries.deleteByFilter(f) returns Future(1)

      Delete("/discussion/1/post", secret) ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        verify(modules.extendedPostQueries).deleteByFilter(f)
      }
    }

    "find all posts for discussion" in new Mocks {
      def f(l: Long)(x: Posts): Rep[Boolean] = x.id === l
      val p = rwalerow.domain.Post(nick = Nick("nick"),
        contents = Contents("contents"),
        email = Email("a@gmail.com"),
        createDate = Timestamp.valueOf(LocalDateTime.now()),
        secret = Secret("abc"),
        discussionId = 1L)
      modules.extendedPostQueries.findByFilter(f(1)) returns Future(List(p))

      Get("/discussion/1/posts") ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "update post based on secret" in new Mocks {
      val secret = Secret("abcdefg")
      val contents = Contents("new contetns")
      modules.extendedPostQueries.updateBySecret(secret, contents) returns Future(1)

      Post(s"/discussion/1/post/${secret.value}", contents) ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }
  }
}