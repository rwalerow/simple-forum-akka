package rwalerow.rest

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{MalformedRequestContentRejection, RejectionHandler}
import akka.http.scaladsl.server.directives.RouteDirectives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import rwalerow.domain.JsonProtocol._

object RejectionHandlers {

  val baseMessage = "json entity with format required "
  val contentsMessage = "{ contents: 'contents' }"
  val createDiscussionMessage = "{ subject: 'contents', contents: 'contents', nick: 'nick', email:'email' }"
  val createPostMessage = "{ contents: 'contents', nick: 'nick', email:'email' }"

  def handlerWithMessage(message: String) = RejectionHandler.newBuilder()
    .handle {
      case MalformedRequestContentRejection(_,_) =>
        complete(BadRequest -> ErrorResponse(BadRequest, baseMessage + message))
    }.result()
}
