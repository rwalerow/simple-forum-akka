package rwalerow.rest

import java.sql.Timestamp
import java.time.LocalDateTime

import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import rwalerow.domain.JsonProtocol._
import akka.http.scaladsl.model.StatusCodes._
import org.scalatest.Matchers
import rwalerow.domain._
import rwalerow.rest.RejectionHandlers._

import scala.concurrent.Future
import slick.driver.PostgresDriver.api._

class RoutesSpec extends AbstractMockedConfigTest with Matchers {

  trait Mocks {
    def actorRefFactory = system
    val modules = new Modules{}
    val routes = new Routes(modules).routes
  }

  "Discussion routes" should {

    "return empty array of discussions" in new Mocks {
      modules.discussionQueries.listDiscussionByPostDates _ expects (*, *) returning Future(List())
      modules.conf.getInt _  expects * returning 10

      Get("/discussions") ~> routes ~> check {
        handled shouldEqual true
        responseAs[List[Discussion]].isEmpty shouldEqual true
      }
    }

    "pass url limit to listDiscussions" in new Mocks {
      modules.discussionQueries.listDiscussionByPostDates _ expects (2, 0) returning Future(List())
      modules.conf.getInt _  expects * returning 10

      Get("/discussions?limit=2") ~> routes ~> check {
        handled shouldEqual true
      }
    }

    "valid discussion be created" in new Mocks {
      val validCreateDiscussion = CreateDiscussion("subject", "contents", "nick", "email@gmail.com")
      modules.discussionQueries.createDiscussion _ expects (*, *) returning Future(Secret("secret"))

      Post("/discussion", validCreateDiscussion) ~> routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
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

      modules.postQueries.createPost _ expects * returning Future(Secret("secret"))

      Post("/discussion/1/post", validPost) ~> routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
        responseAs[Secret] shouldBe Secret("secret")
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
      modules.postQueries.deletePost _ expects (1L, Secret(secret)) returning Future(1)

      Delete("/discussion/1/post/" + secret, secret) ~> routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "find all posts for discussion" in new Mocks {
      val date = LocalDateTime.now()
      val p = rwalerow.domain.Post(id = Some(1),
        nick = Nick("nick"),
        contents = Contents("contents"),
        email = Email("a@gmail.com"),
        createDate = Timestamp.valueOf(date),
        secret = Secret("abc"),
        discussionId = 1L)

      val postAfter = rwalerow.domain.Post(id = Some(2),
        nick = Nick("nick 2"),
        contents = Contents("contents 2"),
        email = Email("abcd@gmail.com"),
        createDate = Timestamp.valueOf(date.plusDays(1)),
        secret = Secret("abc"),
        discussionId = 1L)


      modules.conf.getInt _ expects * returning 10
      modules.postQueries.postWithIndex _ expects (1, 1) returning Future(Some((p, 1)))
      modules.postQueries.countBefore _ expects (1, p.createDate) returning Future(0)
      modules.postQueries.countAfter _ expects (1, p.createDate) returning Future(1)
      modules.postQueries.listPostsWithLimits _ expects (*, *) returns Future(List(postAfter))

      Get("/discussion/1/posts/1") ~> routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "update post based on secret" in new Mocks {
      val secret = Secret("abcdefg")
      val contents = Contents("new contetns")
      modules.postQueries.updateBySecret _ expects (1L, secret, contents) returns Future(1)

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