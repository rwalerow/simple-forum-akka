package rwalerow.rest

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import org.mockito.Mockito._
import org.scalatest.Matchers
import rwalerow.domain.JsonProtocol._
import rwalerow.domain._
import rwalerow.rest.RejectionHandlers._
import org.specs2.matcher.AnyMatchers

import scala.concurrent.Future
import slick.driver.PostgresDriver.api._

class RoutesSpec extends AbstractRestTest with Matchers with AnyMatchers {

  trait Mocks {
    def actorRefFactory = system
    val modules = new Modules {}
    val routes = new Routes(modules).routes
  }

  "Discussion routes" should {

    "return empty array of discussions" in new Mocks {
      modules.discussionQueries.listDiscussionByPostDates(anyInt, anyInt) returns Future(List())
      modules.conf.getInt(anyString) returns 10

      Get("/discussions") ~> routes ~> check {
        handled shouldEqual true
        responseAs[List[Discussion]].isEmpty shouldEqual true
      }
    }

    "pass url limit to listDiscussions" in new Mocks {
      modules.discussionQueries.listDiscussionByPostDates(anyInt, anyInt) returns Future(List())
      modules.conf.getInt(anyString) returns 10

      Get("/discussions?limit=2") ~> routes ~> check {
        handled shouldEqual true
        verify(modules.discussionQueries).listDiscussionByPostDates(limit = argThat(be_==(2)), offset = anyInt)
      }
    }

    "pass config limit to listDiscussions" in new Mocks {
      modules.discussionQueries.listDiscussionByPostDates(anyInt, anyInt) returns Future(List())
      modules.conf.getInt(anyString) returns 10

      Get("/discussions?limit=22") ~> routes ~> check {
        handled shouldEqual true
        verify(modules.discussionQueries).listDiscussionByPostDates(limit = argThat(be_==(10)), offset = anyInt)
      }
    }

    "valid discussion be created" in new Mocks {
      modules.discussionQueries.insert(any[Discussion]) returns Future(1)
      modules.postQueries.insert(any[Post]) returns Future(1)

      Post("/discussion", CreateDiscussion("subject", "contents", "nick", "email@gmail.com")) ~> routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
        verify(modules.postQueries).insert(any[Post])
        verify(modules.discussionQueries).insert(any[Discussion])
      }
    }

    "invalid discussion be filtered" in new Mocks {
      val invalidDiscussion = CreateDiscussion("subject", "contents", "thisisrealytolongnickforittobetrueandnoteasytoremember", "gmail.com")
      Post("/discussion", invalidDiscussion) ~> routes ~> check {
        handled shouldEqual true
        status shouldEqual BadRequest
        responseAs[ErrorResponse] shouldEqual ErrorResponse(BadRequest, "Invalid address email format, Nick is to long")
      }
    }

    "correctly respond to insert without body" in new Mocks {
      Post("/discussion") ~> routes ~> check {
        handled shouldBe true
        response.status shouldEqual BadRequest
        responseAs[ErrorResponse] shouldEqual ErrorResponse(BadRequest, baseMessage + createDiscussionMessage)
      }
    }
  }

  "Post routes" should {

    "create a valid post" in new Mocks {
      val validPost = CreatePost("contents", "nick", "email@gmail.com")
      val discussion = Discussion(Some(1), Subject("subject"))
      modules.postQueries.insert(any[Post]) returns Future(1)
      modules.discussionQueries.findById(anyLong) returns Future(Some(discussion))

      Post("/discussion/1/post", validPost) ~> routes ~> check {
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

    "correctly reject create without entity" in new Mocks {
      Post("/discussion/1/post") ~> routes ~> check {
        handled shouldBe true
        response.status shouldEqual BadRequest
        responseAs[ErrorResponse] shouldEqual ErrorResponse(BadRequest, baseMessage + createPostMessage )
      }
    }

    "delete valid post" in new Mocks {
      val secret = "abcdefghijklmnoprstuwxyz"
      def f(x: Posts): Rep[Boolean] = x.id === 1L && x.secret === Secret(secret)
      modules.postQueries.deleteByFilter(f) returns Future(1)

      Delete("/discussion/1/post/" + secret, secret) ~> routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        verify(modules.postQueries).deleteByFilter(f)
      }
    }

    "find all posts for discussion" in new Mocks {
      val p = rwalerow.domain.Post(id = Some(1),
        nick = Nick("nick"),
        contents = Contents("contents"),
        email = Email("a@gmail.com"),
        createDate = Timestamp.valueOf(LocalDateTime.now()),
        secret = Secret("abc"),
        discussionId = 1L)
      modules.postQueries.countBefore(anyInt, any[Timestamp]) returns Future(0)
      modules.postQueries.countAfter(anyInt, any[Timestamp]) returns Future(0)
      modules.postQueries.postWithIndex(anyLong, anyLong) returns Future(Some((p, 0)))
      modules.postQueries.findInRange(anyInt, anyInt, anyLong, anyLong) returns Future(List(p))

      Get("/discussion/1/posts/1") ~> routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "update post based on secret" in new Mocks {
      val secret = Secret("abcdefg")
      val contents = Contents("new contetns")
      modules.postQueries.updateBySecret(1L, secret, contents) returns Future(1)

      Put(s"/discussion/1/post/${secret.value}", contents) ~> routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "correctly respond to update without body" in new Mocks {
      Put("/discussion/1/post/somesecret") ~> routes ~> check {
        handled shouldBe true
        response.status shouldEqual BadRequest
        responseAs[ErrorResponse] shouldEqual ErrorResponse(BadRequest, baseMessage + contentsMessage)
      }
    }
  }
}