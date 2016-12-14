package rwalerow.rest

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.scalatest.{BeforeAndAfterEach, Matchers}
import rwalerow.domain.JsonProtocol._
import rwalerow.domain.{Post => DPost, _}
import slick.driver.PostgresDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

class RoutesIntegrationSpec extends AbstractIntegrationTest with Matchers with BeforeAndAfterEach {

  val modules = new Modules {}
  val routes = new Routes(modules).routes

  val baseDiscussion = Discussion(subject = Subject("subject"))
  val basePost = DPost(nick = Nick("nick"),
    contents = Contents("contents"),
    email = Email("email@gmail.com"),
    createDate = Timestamp.valueOf(LocalDateTime.now()),
    secret = Secret("secret"),
    discussionId = 0L)

  override def afterEach() = {
    Await.result(for {
    _ <- modules.postQueries.deleteByFilter(_.id != None)
      a <- modules.discussionQueries.deleteByFilter(_.id != None)
    } yield a, 1.second)
  }


  "Discussion routes" should {

    "list discussions properly" in {
      Await.result(for {
        id <- modules.discussionQueries.insert(baseDiscussion)
        r <- modules.postQueries.insert(basePost.copy(discussionId = id))
      } yield r, 1.second)

      Get("/discussions") ~> routes ~> check {
        handled shouldEqual true
        responseAs[List[Discussion]].head.subject shouldEqual Subject("subject")
      }
    }

    "list 2 discussions in correct order" in {
      val insertDiscussion2 = Discussion(subject = Subject("subject2"))
      val timeNow = LocalDateTime.now()

      Await.result(for {
        id <- modules.discussionQueries.insert(baseDiscussion)
        _ <- modules.postQueries.insert(basePost.copy(discussionId = id))
        secondId <- modules.discussionQueries.insert(insertDiscussion2)
        r <- modules.postQueries.insert(basePost.copy(discussionId = secondId, createDate = Timestamp.valueOf(timeNow.plusMinutes(2L))))
      } yield r, 1.second)

      Get("/discussions") ~> routes ~> check {
        handled shouldEqual true
        responseAs[List[Discussion]].map(_.subject) should contain theSameElementsInOrderAs List(Subject("subject2"), Subject("subject"))
      }
    }

    "insert discussions properly" in {
      val discussion = CreateDiscussion("subject1", "contents1", "nick1", "email1@gmail.com")

      Post("/discussion", discussion) ~> routes ~> check {
        handled shouldEqual true
        val counts = for {
          dCount <- modules.discussionQueries.list
          pCount <- modules.postQueries.list
        } yield (dCount, pCount)

        val (dCount, pCount) = Await.result(counts, 1.second)
        dCount.size shouldEqual 1
        pCount.size shouldEqual 1
      }
    }
  }

  "Post routes" should {

    "insert post" in {
      val discId = Await.result(for {

        id <- modules.discussionQueries.insert(baseDiscussion)
        _ <- modules.postQueries.insert(basePost.copy(discussionId = id))
      } yield id, 1.second)
      
      val createPost = CreatePost(contents = "contents", nick = "nick", email = "email@gmail.com")

      Post(s"/discussion/$discId/post", createPost) ~> routes ~> check {
        val discussionPosts = Await.result(modules.postQueries.findByFilter(_.discussionId === discId), 1.second)

        discussionPosts.toList.size shouldEqual 2
      }
    }

    "properly list posts" in {
      val timeNow = LocalDateTime.now()
      def insertPostWith(post: DPost, nick: String, secondBefore: Long) =
        modules.postQueries.insert(
          post.copy(nick = Nick(nick), createDate = Timestamp.valueOf(timeNow.minusDays(secondBefore)))
        )

      val postForTest = Await.result(for {
        dId   <- modules.discussionQueries.insert(baseDiscussion)
        extPost = basePost.copy(discussionId = dId)
        _     <- insertPostWith(extPost, "1", 8L)
        _     <- insertPostWith(extPost, "2", 7L)
        postId<- insertPostWith(extPost, "3", 6L)
        _     <- insertPostWith(extPost, "4", 5L)
        _     <- insertPostWith(extPost, "5", 4L)
        _     <- insertPostWith(extPost, "6", 3L)
        _     <- insertPostWith(extPost, "7", 2L)
        _     <- insertPostWith(extPost, "8", 1L)
        post <- modules.postQueries.findById(postId)
      } yield post, 1.second)

      postForTest.isDefined shouldEqual true
      val post = postForTest.get
      val postId = post.id.getOrElse(0L)

      Get(s"/discussion/${post.discussionId}/posts/$postId") ~> routes ~> check {
        handled shouldEqual true
        responseAs[List[DPost]].size shouldEqual 4
        responseAs[List[DPost]].map(_.nick.value) should contain theSameElementsInOrderAs List("2", "3", "4", "5")
      }
    }
  }
}