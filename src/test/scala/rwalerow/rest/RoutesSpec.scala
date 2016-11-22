package rwalerow.rest

import rwalerow.domain.{Discussion, Post}

import scala.concurrent.Future
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import org.mockito.Mockito._
import org.scalatest.Matchers
import rwalerow.domain.JsonProtocol._

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
}