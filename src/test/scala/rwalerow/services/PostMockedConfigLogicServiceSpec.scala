package rwalerow.services

import org.scalatest.Matchers
import rwalerow.domain.Secret
import rwalerow.rest.{AbstractMockedConfigTest, CreatePost, Routes}

import scala.concurrent.Future

class PostMockedConfigLogicServiceSpec extends AbstractMockedConfigTest with Matchers {

  def actorRefFactory = system
  val modules = new Modules {}
  val routes = new Routes(modules).routes
  val postService = new PostRestLogicService(modules)

  "Create post" should {
    "call create post query" in {
      modules.postQueries.createPost _ expects * returning Future(Secret("secret"))
      val validPost = CreatePost("contents", "nick", "email@gmail.com")

      postService.createPost(validPost,1L)
    }
  }
}