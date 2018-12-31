package rwalerow.rest

import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import cats.data.Validated.{Invalid, Valid}
import rwalerow.domain._
import rwalerow.utils.{Configuration, PersistenceModule, RestLogicServices}
import rwalerow.domain.JsonProtocol._
import rwalerow.rest.RejectionHandlers._

import slick.driver.PostgresDriver.api._

import scala.util.{Failure, Success}

class Routes(modules: Configuration with PersistenceModule with RestLogicServices) extends Directives {

  def discussionListRoute = (get & pathPrefix("discussions") & parameters('limit.as[Int].?, 'offset.as[Int].?) & pathEnd) { (limit, offset) =>
    onComplete(modules.discussionLogicService.listDiscussionByPostDates(limit, offset)) {
      case Success(discussions) => complete(discussions)
      case Failure(err) => complete(InternalServerError ->  ErrorResponse(InternalServerError, err.getMessage))
    }
  }

  def createDiscussionRoute = pathPrefix("discussion") {
    (pathEnd & post & handleRejections(handlerWithMessage(createDiscussionMessage)) & entity(as[CreateDiscussion])) { createD =>
      CreateDiscussion.validate(createD) match {
        case Valid(createDiscussion) =>
          onComplete(modules.discussionLogicService.createDiscussion(createDiscussion)) {
            case Success(secret) => complete(Created -> secret)
            case Failure(err) => complete(InternalServerError -> ErrorResponse(InternalServerError, err.getMessage))
          }
        case Invalid(errors) => complete(BadRequest -> ErrorResponse(BadRequest, errors))
      }
    }
  }

  def getPostsRoute = (path("discussion" / LongNumber / "posts" / LongNumber) & get) { (discussionId, postId) =>
    onComplete(modules.postLogicService.listPosts(discussionId, postId)) {
      case Success(postsResult) => complete(postsResult)
      case Failure(err) => complete(InternalServerError -> ErrorResponse(InternalServerError, err.getMessage))
    }
  }

  private val postRoutePrefix = pathPrefix("discussion" / LongNumber / "post")

  def createPostRoute = (postRoutePrefix & post) { discussionId =>
    (handleRejections(handlerWithMessage(createPostMessage)) & entity(as[CreatePost])) { createP =>
      CreatePost.validate(createP) match {
        case Valid(createPost) =>
          onComplete(modules.postLogicService.createPost(createPost, discussionId)) {
            case Success(secret) => complete(Created -> secret)
            case Failure(err) => complete(InternalServerError -> ErrorResponse(InternalServerError, err.getMessage))
          }
        case Invalid(errors) => complete(BadRequest -> ErrorResponse(BadRequest, errors))
      }
    }
  }

  def deletePostRoute = (postRoutePrefix & path(Segment) & delete) { (discussionId, secret) =>
    onComplete(modules.postLogicService.deletePost(discussionId, Secret(secret))) {
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
