package rwalerow.rest

import cats.data.ValidatedNel
import cats.data.Validated._
import cats.data.{NonEmptyList => NEL}
import rwalerow.domain.{Email, Nick, Subject}
import cats.syntax.cartesian._

case class CreateDiscussion(subject: String, contents: String, nick: String, email: String)

object CreateDiscussion {

  def validateCreateDiscussion(createDiscussion: CreateDiscussion): ValidatedNel[String, CreateDiscussion] =
    (validateEmail(createDiscussion.email) |@|
    validateNick(createDiscussion.nick) |@|
    validateSubject(createDiscussion.subject)) map {(_,_,_) => createDiscussion}

  private def validateEmail(email: String): ValidatedNel[String, Email] =
    if(Email.isValid(email)) valid(Email(email))
    else invalid(NEL.of("Invalid address email format"))

  private def validateNick(nick: String): ValidatedNel[String, Nick] =
    if(Nick.isValid(nick)) valid(Nick(nick))
    else invalid(NEL.of("Nick is to long"))

  private def validateSubject(subject: String): ValidatedNel[String, Subject] =
    if(Subject.isValid(subject)) valid(Subject(subject))
    else invalid(NEL.of("Subject is to long"))
}