package rwalerow.rest

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import cats.data.Validated.{Invalid, Valid}
import rwalerow.domain._
import rwalerow.utils.{Configuration, PersistenceModule, RestLogicServices}
import rwalerow.domain.JsonProtocol._
import rwalerow.services.PostCalculations
import rwalerow.rest.RejectionHandlers._

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.PostgresDriver.api._

import scala.util.{Failure, Success}

class Routes(modules: Configuration with PersistenceModule with RestLogicServices) extends Directives {

  def discussionListRoute = (pathPrefix("discussions") & pathEnd & get & parameters('limit.as[Int].?, 'offset.as[Int].?)) { (limit, offset) =>

    val maxLimit = modules.config.getInt("limit.discussions")
    val calculatedLimit = limit.filter(_ < maxLimit).getOrElse(maxLimit)
    val calculatedOffset = offset.getOrElse(0)

    onComplete(modules.discussionQueries.listDiscussionByPostDates(calculatedLimit, calculatedOffset)) {
      case Success(discussions) => complete(discussions)
      case Failure(err) => complete(InternalServerError ->  ErrorResponse(InternalServerError, err.getMessage))
    }
  }

  def createDiscussionRoute = pathPrefix("discussion") {
    (pathEnd & post & handleRejections(handlerWithMessage(createDiscussionMessage)) & entity(as[CreateDiscussion])) { createD =>
      CreateDiscussion.validate(createD) match {
        case Valid(createDiscussion) =>
          onComplete(modules.discussionService.createDiscussion(createDiscussion)) {
            case Success(secret) => complete(Created -> secret)
            case Failure(err) => complete(InternalServerError -> ErrorResponse(InternalServerError, err.getMessage))
          }
        case Invalid(errors) => complete(BadRequest -> ErrorResponse(BadRequest, errors))
      }
    }
  }

  def getPostsRoute = (path("discussion" / LongNumber / "posts" / LongNumber) & get) { (discussionId, postId) =>

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

  val postRoutePrefix = pathPrefix("discussion" / LongNumber / "post")

  def createPostRoute = (postRoutePrefix & post) { discussionId =>
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

  def deletePostRoute = (postRoutePrefix & path(Segment) & delete) { (discussionId, secret) =>
    onComplete(modules.postQueries.deleteByFilter{ x => x.discussionId === discussionId && x.secret === Secret(secret)}) {
      case Success(_) => complete(HttpResponse(OK))
      case Failure(err) => complete(InternalServerError -> ErrorResponse(InternalServerError, err.getMessage))
    }
  }

  def updatePostRoute = (postRoutePrefix & path(Segment)) { (discussionId, secret) =>
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

  val routes = discussionListRoute ~ createDiscussionRoute ~ getPostsRoute ~ createPostRoute ~ deletePostRoute ~ updatePostRoute
}
