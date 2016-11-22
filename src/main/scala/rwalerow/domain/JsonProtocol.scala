package rwalerow.domain

import java.sql.Timestamp
import java.time.LocalDateTime

import rwalerow.rest.CreateDiscussion
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

import scala.util.{Failure, Success, Try}

/**
  * Created by robert on 21.11.16.
  */
object JsonProtocol extends DefaultJsonProtocol {

  implicit val nickProtocol = jsonFormat1(Nick.apply)
  implicit val contentsProtocol = jsonFormat1(Contents)
  implicit val emailProtocol = jsonFormat1(Email.apply)
  implicit val secretProtocol = jsonFormat1(Secret.apply)
  implicit val subjectProtocol = jsonFormat1(Subject.apply)
  implicit val discussionProtocol = jsonFormat2(Discussion)
  implicit object TimestampProtocol extends RootJsonFormat[Timestamp] {
    override def write(obj: Timestamp): JsValue = JsString(obj.toString)

    override def read(json: JsValue): Timestamp = json match {
      case JsString(s) => Try(Timestamp.valueOf(s)) match {
        case Success(x) => x
        case Failure(x) => Timestamp.valueOf(LocalDateTime.MIN)
      }
      case _ => Timestamp.valueOf(LocalDateTime.MIN)
    }
  }

  implicit val postProtocol = jsonFormat7(Post)

  // Request protocols
  implicit val createDiscussionProtocol = jsonFormat4(CreateDiscussion.apply)
}