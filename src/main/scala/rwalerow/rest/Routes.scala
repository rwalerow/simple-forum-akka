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
      case Failure(err) => complete(InternalServerError, s"Error occurred ${err.getMessage}")
    }
  }

  def discussionCreateRoute = pathPrefix("discussion") {
    (pathEnd & put & entity(as[CreateDiscussion])) { createD =>
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
            createdPostId <- modules.extendedPostQueries.insert(post.copy(discussionId = createdId))
          } yield createdPostId

          onComplete(response) {
            case Success(_) => complete(HttpResponse(status = Created, entity = post.secret.value))
            case Failure(err) => complete(HttpResponse(status = BadRequest, entity = err.getMessage))
          }
        case Invalid(errors) => complete(BadRequest, errors.toList)
      }
    }
  }

  val postRoute = pathPrefix("discussion" / LongNumber / "post")

  def getPosts = (path("discussion" / LongNumber / "posts") & get) { discussionId =>
    onComplete(modules.extendedPostQueries.findByFilter{_.discussionId === discussionId}) {
      case Success(posts) => complete(posts)
      case Failure(err) => complete(InternalServerError, s"Error occurred ${err.getMessage}")
    }
  }

  def createPost = (postRoute & put) { discussionId =>
    entity(as[CreatePost]) { createP =>
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
            createPostId <- modules.extendedPostQueries.insert(post)
          } yield createPostId

          onComplete(query) {
            case Success(_) => complete(HttpResponse(status = Created, entity = post.secret.value))
            case Failure(err) => complete(HttpResponse(status = BadRequest, entity = err.getMessage))
          }
        case Invalid(errors) => complete(BadRequest, errors.toList)
      }
    }
  }

  def deletePost = (postRoute & delete) { discussionId =>
    entity(as[Secret]) { secret =>
      onComplete(modules.extendedPostQueries.deleteByFilter{ x => x.discussionId === discussionId && x.secret === secret}) {
        case Success(_) => complete(HttpResponse(status = OK))
        case Failure(err) => complete(HttpResponse(status = BadRequest, entity = err.getMessage))
      }
    }
  }

  def updatePost = (postRoute & path(Segment)){ (discussionId, secret) =>
      post {
        entity(as[Contents]) { contents =>
          onComplete(modules.extendedPostQueries.updateBySecret(Secret(secret), contents)) {
            case Success(_) => complete(HttpResponse(OK))
            case Failure(err) => complete(HttpResponse(status = BadRequest, entity = err.getMessage))
          }
        }
      }
  }

  val routes = discussionListRoute ~ discussionCreateRoute ~ getPosts ~ createPost ~ deletePost ~ updatePost
}
