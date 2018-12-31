package rwalerow.services

import java.sql.Timestamp
import java.time.LocalDateTime

import rwalerow.domain._
import rwalerow.rest.CreatePost
import rwalerow.utils.{Configuration, PersistenceModule}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostRestLogicService(modules: Configuration with PersistenceModule) {

  def createPost(createPost: CreatePost, discussionId: Long): Future[Secret] = {
    val post = Post(
      nick = Nick(createPost.nick),
      contents = Contents(createPost.contents),
      email = Email(createPost.email),
      secret = Secret(Secret.generate),
      createDate = Timestamp.valueOf(LocalDateTime.now()),
      discussionId = discussionId
    )

    modules.postQueries.createPost(post)
  }

  def listPosts(discussionId: Long, postId: Long): Future[Seq[Post]] = {

    val limit = modules.config.getInt("limit.posts")

    for {
      Some((post, index))     <- modules.postQueries.postWithIndex(discussionId, postId)
      before                  <- modules.postQueries.countBefore(discussionId, post.createDate)
      after                   <- modules.postQueries.countAfter(discussionId, post.createDate)
      (takeBefore, _) = if((before + after + 1) > limit) PostCalculations.calculateBeforeAndAfter(before, after, limit) else (before, after)
      posts                   <- modules.postQueries.listPostsWithLimits(takeBefore, limit)
    } yield posts
  }

  def deletePost(discussionId: Long, secret: Secret): Future[Int] = modules.postQueries.deletePost(discussionId, secret)

}
