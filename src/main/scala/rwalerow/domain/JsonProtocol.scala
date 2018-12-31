package rwalerow.domain

import java.sql.Timestamp
import java.time.LocalDateTime

import play.api.libs.json._
import rwalerow.rest.{CreateDiscussion, CreatePost, ErrorResponse}

import scala.util.{Failure, Success, Try}

object JsonProtocol {

  implicit val nickProtocol = Json.format[Nick]
  implicit val contentsProtocol = Json.format[Contents]
  implicit val emailProtocol = Json.format[Email]
  implicit val secretProtocol = Json.format[Secret]
  implicit val subjectProtocol = Json.format[Subject]
  implicit val discussionProtocol = Json.format[Discussion]

  implicit val timestampProtocol = new Format[Timestamp] {

    override def reads(json: JsValue): JsResult[Timestamp] = json match {
      case JsString(s) => Try(Timestamp.valueOf(s)) match {
        case Success(x) => JsSuccess(x)
        case Failure(ex) => JsError(ex.getMessage)
      }
      case _ => JsError("Invalid filed parsing")
    }

    override def writes(o: Timestamp): JsValue = JsString(o.toString)
  }

  implicit val postProtocol = Json.format[Post]

  // Request protocols
  implicit val createDiscussionProtocol = Json.format[CreateDiscussion]
  implicit val createPostProtocol = Json.format[CreatePost]
  implicit val errorResponseProtocol = Json.format[ErrorResponse]
}