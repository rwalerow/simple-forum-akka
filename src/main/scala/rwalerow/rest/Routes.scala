package rwalerow.rest

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import cats.data.Validated.{Invalid, Valid}
import rwalerow.domain._
import rwalerow.utils.{Configuration, PersistenceModule}
import rwalerow.domain.JsonProtocol._
import rwalerow.services.PostCalculations
import rwalerow.rest.RejectionHandlers._

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.PostgresDriver.api._

import scala.util.{Failure, Success}

class Routes(modules: Configuration with PersistenceModule) extends Directives {

  def discussionListRoute = (pathPrefix("discussions") & pathEnd & get & parameters('limit.as[Int].?, 'offset.as[Int].?)) { (limit, offset) =>

    val maxLimit = modules.config.getInt("limit.discussions")
    val calculatedLimit = limit.filter(_ < maxLimit).getOrElse(maxLimit)
    val calculatedOffset = offset.getOrElse(0)

    onComplete(modules.discussionQueries.listDiscussionByPostDates(calculatedLimit, calculatedOffset)) {
      case Success(discussions) => complete(discussions)
      case Failure(err) => complete(InternalServerError ->  ErrorResponse(InternalServerError, err.getMessage))
    }
  }

  def discussionCreateRoute = pathPrefix("discussion") {
    (pathEnd & post & handleRejections(handlerWithMessage(createDiscussionMessage)) & entity(as[CreateDiscussion])) { createD =>
      CreateDiscussion.validate(createD) match {
        case Valid(createDiscussion) =>
          val discussion = Discussion(subject = Subject(createDiscussion.subject))
          val post = Post(
            nick = Nick(createDiscussion.nick),
            contents = Contents(createDiscussion.contents),
            email = Email(createDiscussion.email),
            secret = Secret(Secret.generate),
            createDate = Timestamp.valueOf(LocalDateTime.now()),
            discussionId = 0L
          )
          val response = for {
            createdId <- modules.discussionQueries.insert(discussion)
            createdPostId <- modules.postQueries.insert(post.copy(discussionId = createdId))
          } yield createdPostId

          onComplete(response) {
            case Success(_) => complete(Created -> post.secret)
            case Failure(err) => complete(InternalServerError -> ErrorResponse(InternalServerError, err.getMessage))
          }
        case Invalid(errors) => complete(BadRequest -> ErrorResponse(BadRequest, errors))
      }
    }
  }

  def getPosts = (path("discussion" / LongNumber / "posts" / LongNumber) & get) { (discussionId, postId) =>

    val configLimit = modules.config.getInt("limit.posts")

    onComplete(modules.postQueries.postWithIndex(discussionId, postId)) {
      case Success(Some((post, index))) =>
        val responseQuery = for {
          before                  <- modules.postQueries.countBefore(discussionId, post.createDate)
          after                   <- modules.postQueries.countAfter(discussionId, post.createDate)
          (takeBefore, takeAfter) = if((before + after + 1) > configLimit) PostCalculations.calculateBeforeAndAfter(before, after, configLimit) else (before, after)
          posts                   <- modules.postQueries.findInRange(takeBefore, takeAfter, index, discussionId)
        } yield posts
        onComplete(responseQuery) {
          case Success(postsResult) => complete(postsResult)
          case Failure(err) => complete(InternalServerError -> ErrorResponse(InternalServerError, err.getMessage))
        }
      case Success(None) => complete(BadRequest -> ErrorResponse(BadRequest, "Post not found"))
      case Failure(err) => complete(InternalServerError -> ErrorResponse(InternalServerError, err.getMessage))
    }
  }

  val postRoute = pathPrefix("discussion" / LongNumber / "post")

  def createPost = (postRoute & post) { discussionId =>
    (handleRejections(handlerWithMessage(createPostMessage)) & entity(as[CreatePost])) { createP =>
      CreatePost.validate(createP) match {
        case Valid(createPost) =>
          val post = Post(
            nick = Nick(createPost.nick),
            contents = Contents(createPost.contents),
            email = Email(createPost.email),
            secret = Secret(Secret.generate),
            createDate = Timestamp.valueOf(LocalDateTime.now()),
            discussionId = discussionId
          )

          val query = for {
            exists <- modules.discussionQueries.findById(discussionId) if exists.isDefined
            createPostId <- modules.postQueries.insert(post)
          } yield createPostId

          onComplete(query) {
            case Success(_) => complete(Created -> post.secret)
            case Failure(err) => complete(InternalServerError -> ErrorResponse(InternalServerError, err.getMessage))
          }
        case Invalid(errors) => complete(BadRequest -> ErrorResponse(BadRequest, errors))
      }
    }
  }

  def deletePost = (postRoute & path(Segment) & delete) { (discussionId, secret) =>
    onComplete(modules.postQueries.deleteByFilter{ x => x.discussionId === discussionId && x.secret === Secret(secret)}) {
      case Success(_) => complete(HttpResponse(OK))
      case Failure(err) => complete(InternalServerError -> ErrorResponse(InternalServerError, err.getMessage))
    }
  }

  def updatePost = (postRoute & path(Segment)) { (discussionId, secret) =>
      put {
        (handleRejections(handlerWithMessage(contentsMessage)) & entity(as[Contents])) { contents =>
          onComplete(modules.postQueries.updateBySecret(discussionId, Secret(secret), contents)) {
            case Success(0) => complete(NotFound -> ErrorResponse(NotFound, s"Post with secret:$secret not found in discussion id:$discussionId"))
            case Success(_) => complete(HttpResponse(OK))
            case Failure(err) => complete(InternalServerError -> ErrorResponse(InternalServerError, err.getMessage))
          }
        }
      }
  }

  val routes = discussionListRoute ~ discussionCreateRoute ~ getPosts ~ createPost ~ deletePost ~ updatePost
}
