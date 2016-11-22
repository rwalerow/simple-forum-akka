package rwalerow.domain

import java.sql.Timestamp

import rwalerow.utils.BaseEntity
import slick.driver.PostgresDriver.api._

import scala.util.Random

/**
  * Domain
  */
case class Email(value: String) extends MappedTo[String] {
  require(Email.isValid(value), "Invalid address email format")
}
case class Nick(value: String) extends MappedTo[String] {
  require(Nick.isValid(value), "Nick is to long")
}
case class Secret(value: String) extends MappedTo[String]
case class Contents(value: String) extends MappedTo[String]
case class Post(id: Option[Long] = None, nick: Nick, contents: Contents, email: Email, createDate: Timestamp, secret: Secret, discussionId: Long) extends BaseEntity


case class Subject(value: String) extends MappedTo[String] {
  require(Subject.isValid(value), "Subject is to long")
}
case class Discussion(id: Option[Long] = None, subject: Subject) extends BaseEntity


/**
  * Companion objects
  */
object Email {
  def isValid(email: String): Boolean = EmailRegex.pattern.matcher(email.toUpperCase).matches()

  private val EmailRegex = """\b[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*\b""".r
}
object Nick {
  def isValid(nick: String): Boolean = nick.length < 40
}
object Subject {
  def isValid(subject: String): Boolean = subject.length < 255
}
object Secret {
  val secretLength = 40
  def generate: String = Random.alphanumeric.take(secretLength).mkString
}