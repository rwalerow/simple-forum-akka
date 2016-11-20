package rwalerow.domain

import java.sql.Date

import rwalerow.utils.{BaseEntity, BaseTable}
import slick.driver.PostgresDriver.api._

case class Email(value: String) extends MappedTo[String] {
  require(Email.isValid(value), "Invalid address email format")
}
case class Nick(value: String) extends MappedTo[String] {
  require(value.length < 40, "Nick is to long")
}
case class Secret(value: String) extends MappedTo[String]
case class Contents(value: String) extends MappedTo[String]
case class Subject(value: String) extends MappedTo[String] {
  require(value.length < 255, "Subject is to long")
}
case class CreateDate(value: Date) extends MappedTo[Date]
case class Post(id: Long, nick: Nick, contents: Contents, email: Email, createDate: CreateDate, secret: Secret) extends BaseEntity

object Email {
  def isValid(email: String): Boolean = EmailRegex.pattern.matcher(email.toUpperCase).matches()

  private val EmailRegex = """\b[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*\b""".r
}

class Posts(tag: Tag) extends BaseTable[Post](tag, "posts") {
  def nick        = column[Nick]("nick")
  def contents    = column[Contents]("contents")
  def email       = column[Email]("email")
  def createDate  = column[CreateDate]("create_date")
  def secret      = column[Secret]("secret")
  override def *  = (id, nick, contents, email, createDate, secret) <> (Post.tupled, Post.unapply)
}

