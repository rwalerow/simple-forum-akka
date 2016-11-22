package rwalerow.domain

import java.sql.Timestamp
import rwalerow.utils.BaseEntity
import slick.driver.PostgresDriver.api._


case class Email(value: String) extends MappedTo[String] {
  require(Email.isValid(value), "Invalid address email format")
}
case class Nick(value: String) extends MappedTo[String] {
  require(value.length < 40, "Nick is to long")
}
case class Secret(value: String) extends MappedTo[String]
case class Contents(value: String) extends MappedTo[String]
case class Post(id: Option[Long], nick: Nick, contents: Contents, email: Email, createDate: Timestamp, secret: Secret) extends BaseEntity

object Email {
  def isValid(email: String): Boolean = EmailRegex.pattern.matcher(email.toUpperCase).matches()

  private val EmailRegex = """\b[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*\b""".r
}

case class Subject(value: String) extends MappedTo[String] {
  require(value.length < 255, "Subject is to long")
}
case class Discussion(id: Option[Long], subject: Subject) extends BaseEntity