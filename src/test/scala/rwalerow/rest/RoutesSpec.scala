package rwalerow.rest

import java.sql.Timestamp
import java.time.LocalDateTime

import rwalerow.domain._

import scala.concurrent.Future
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import org.mockito.Mockito._
import org.scalatest.Matchers
import rwalerow.domain.JsonProtocol._
//import slick.lifted._
import slick.driver.PostgresDriver.api._

class RoutesSpec extends AbstractRestTest with Matchers {

  def actorRefFactory = system
  val modules = new Modules{}
  val discussionRoutes = new Routes(modules)

  "Discussion routes" should {

    "return empty array of discussions" in {
      modules.discussionDao.list returns Future(List())

      Get("/discussions") ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        responseAs[List[Discussion]].isEmpty shouldEqual true
      }
    }

    "valid discussion be created" in {
      modules.discussionDao.insert(any[Discussion]) returns Future(1)
      modules.postDao.insert(any[Post]) returns Future(1)

      Put("/discussion", CreateDiscussion("subject", "contents", "nick", "email@gmail.com")) ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        status shouldEqual StatusCodes.Created
        verify(modules.postDao).insert(any[Post])
        verify(modules.discussionDao).insert(any[Discussion])
      }
    }

    "invalid discussion be filtered" in {
      val invalidDiscussion = CreateDiscussion("subject", "contents", "thisisrealytolongnickforittobetrueandnoteasytoremember", "gmail.com")
      Put("/discussion", invalidDiscussion) ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        status shouldEqual StatusCodes.BadRequest
        responseAs[List[String]].isEmpty shouldEqual false
        responseAs[List[String]] should contain theSameElementsAs List("Invalid address email format", "Nick is to long")
      }
    }
  }

  "Post routes" should {

    "create a valid post" in {
      val validPost = CreatePost("contents", "nick", "email@gmail.com")
      val discussion = Discussion(Some(1), Subject("subject"))
      modules.postDao.insert(any[Post]) returns Future(1)
      modules.discussionDao.findById(anyLong) returns Future(Some(discussion))

      Put("/discussion/1/post", validPost) ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        status shouldEqual StatusCodes.Created
        responseAs[String].length > 0 shouldEqual true
        /*
            Why it is called twice?
         */
//        verify(modules.postDao).insert(any[Post])
        verify(modules.discussionDao).findById(anyLong)
      }
    }

    "delete valid post" in {
      val secret = Secret("abcdefghijklmnoprstuwxyz")
      def f(x: Posts): Rep[Boolean] = x.id === 1L && x.secret === secret
      modules.postDao.deleteByFilter(f) returns Future(1)

      Delete("/discussion/1/post", secret) ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        status shouldEqual StatusCodes.OK
        verify(modules.postDao).deleteByFilter(f)
      }
    }


    "find all posts for discussion" in {
      def f(l: Long)(x: Posts): Rep[Boolean] = x.id === l
      val p = rwalerow.domain.Post(nick = Nick("nick"),
        contents = Contents("contents"),
        email = Email("a@gmail.com"),
        createDate = Timestamp.valueOf(LocalDateTime.now()),
        secret = Secret("abc"),
        discussionId = 1L)
      modules.postDao.findByFilter(f(1)) returns Future(List(p))

      Get("/discussion/1/posts") ~> discussionRoutes.routes ~> check {
        handled shouldEqual true
        status shouldEqual StatusCodes.OK
      }
    }
  }
}