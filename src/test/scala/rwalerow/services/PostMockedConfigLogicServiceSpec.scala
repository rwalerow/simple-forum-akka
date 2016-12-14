package rwalerow.services

import org.scalatest.Matchers
import org.specs2.matcher.AnyMatchers
import rwalerow.rest.{AbstractMockedConfigTest, CreatePost, Routes}
import org.mockito.Mockito._
import rwalerow.domain.{Post, Secret}

import scala.concurrent.Future

class PostMockedConfigLogicServiceSpec extends AbstractMockedConfigTest with Matchers with AnyMatchers {

  def actorRefFactory = system
  val modules = new Modules {}
  val routes = new Routes(modules).routes
  val postService = new PostRestLogicService(modules)

  "Create post" should {
    "call create post query" in {
      modules.postQueries.createPost(any[Post]) returns Future(Secret("secret"))
      val validPost = CreatePost("contents", "nick", "email@gmail.com")

      postService.createPost(validPost, 1L)

      verify(modules.postQueries).createPost(any[Post])
    }
  }
}