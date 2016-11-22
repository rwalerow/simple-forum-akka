package rwalerow.rest

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives
import cats.data.Validated.{Invalid, Valid}
import rwalerow.domain._
import rwalerow.utils.{Configuration, PersistenceModule}
import rwalerow.domain.JsonProtocol._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}

class Routes(modules: Configuration with PersistenceModule) extends Directives {

  def discussionListRoute = (pathPrefix("discussions") & pathEnd & get & onComplete(modules.discussionDao.list)) {
    case Success(discussions) => complete(discussions)
    case Failure(err) => complete(StatusCodes.InternalServerError, s"Error occurred ${err.getMessage}")
  }

  def discussionCreateRoute = pathPrefix("discussion") {
    (pathEnd & put & entity(as[CreateDiscussion])) { createD =>
      CreateDiscussion.validateCreateDiscussion(createD) match {
        case Valid(createDiscussion) => {
          val discussion = Discussion(subject = Subject(createDiscussion.subject))
          val post = Post(
            nick = Nick(createD.nick),
            contents = Contents(createD.contents),
            email = Email(createD.email),
            secret = Secret(Secret.generate),
            createDate = Timestamp.valueOf(LocalDateTime.now()),
            discussionId = 0L
          )
          val response = for {
            createdId <- modules.discussionDao.insert(discussion)
            createdPostId <- modules.postDao.insert(post.copy(discussionId = createdId))
          } yield createdPostId

          onSuccess(response) { _ => complete(HttpResponse(status = StatusCodes.Created, entity = post.secret.value)) }
        }
        case Invalid(errors) => complete(StatusCodes.BadRequest, errors.toList)
      }
    }
  }

  val routes = discussionListRoute ~ discussionCreateRoute
}
