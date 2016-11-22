package rwalerow.domain

import java.sql.Timestamp
import rwalerow.utils.BaseTable
import slick.driver.PostgresDriver.api._

class Posts(tag: Tag) extends BaseTable[Post](tag, "posts") {
  def nick        = column[Nick]("nick")
  def contents    = column[Contents]("contents")
  def email       = column[Email]("email")
  def createDate  = column[Timestamp]("create_date")
  def secret      = column[Secret]("secret")
  override def *  = (id.?, nick, contents, email, createDate, secret) <> (Post.tupled, Post.unapply)
}

class Discussions(tag: Tag) extends BaseTable[Discussion](tag, "discussions") {
  def subject     = column[Subject]("subject")
  override def *  = (id.?, subject) <> (Discussion. tupled, Discussion.unapply)
}