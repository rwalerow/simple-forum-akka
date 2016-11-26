package rwalerow.rest

import akka.http.scaladsl.model.StatusCode
import cats.data.ValidatedNel
import cats.data.Validated._
import cats.data.{NonEmptyList => NEL}
import rwalerow.domain.{Contents, Email, Nick, Subject}
import cats.syntax.cartesian._

case class CreateDiscussion(subject: String, contents: String, nick: String, email: String)
case class CreatePost(contents: String, nick: String, email: String)

case class ErrorResponse(code: Int, message: String = "", description: String = "")
object ErrorResponse {
  def apply(status: StatusCode, description: String): ErrorResponse =
    ErrorResponse(status.intValue(), status.reason(), description)

  def apply(status: StatusCode, errors: NEL[String]): ErrorResponse =
    ErrorResponse(status.intValue(), status.reason(), errors.toList.mkString(", "))
}

object CommonValidations {
  def validateEmail(email: String): ValidatedNel[String, Email] =
    if(Email.isValid(email)) valid(Email(email))
    else invalidNel("Invalid address email format")

  def validateNick(nick: String): ValidatedNel[String, Nick] =
    if(Nick.isValid(nick)) valid(Nick(nick))
    else invalidNel("Nick is to long")

  def validateSubject(subject: String): ValidatedNel[String, Subject] =
    if(Subject.isValid(subject)) valid(Subject(subject))
    else invalidNel("Subject is to long")

  def validateContents(contents: String): ValidatedNel[String, Contents] =
    if(Contents.isValid(contents)) valid(Contents(contents))
    else invalidNel("Contents is to long")
}

object CreateDiscussion {

  import CommonValidations._

  def validate(createDiscussion: CreateDiscussion): ValidatedNel[String, CreateDiscussion] =
    (validateEmail(createDiscussion.email) |@|
      validateNick(createDiscussion.nick) |@|
      validateSubject(createDiscussion.subject) |@|
      validateContents(createDiscussion.contents)) map {(_,_,_,_) => createDiscussion}
}

object CreatePost {

  import CommonValidations._

  def validate(createPost: CreatePost): ValidatedNel[String, CreatePost] =
    (validateEmail(createPost.email) |@|
      validateNick(createPost.nick) |@|
      validateContents(createPost.contents)) map {(_,_,_) => createPost}
}
