package rwalerow.domain.common

import rwalerow.utils.{DbModule, Profile}
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import slick.lifted.{CanBeQueryCondition, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * source: https://github.com/cdiniz/slick-akka-http/tree/master/src/main/scala/persistence/entities
  */
trait BaseEntity {
  val id: Option[Long]
}

abstract class BaseTable[T](tag: Tag, name: String) extends Table[T](tag, name){
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
}

trait BaseDao[T, A] {
  def insert(row: A): Future[Long]
  def insert(rows: Seq[A]): Future[Seq[Long]]
  def update(row: A): Future[Int]
  def update(rows: Seq[A]): Future[Unit]
  def findById(id: Long): Future[Option[A]]
  def findByFilter[C : CanBeQueryCondition](f: T => C): Future[Seq[A]]
  def deleteById(id: Long): Future[Int]
  def deleteById(ids: Seq[Long]): Future[Int]
  def deleteByFilter[C: CanBeQueryCondition](f: T => C): Future[Int]
  def list: Future[Seq[A]]
}

trait BaseDBIODao[T, A] {
  def insertQ(row: A): DBIO[Long]
  def insertQ(rows: Seq[A]): DBIO[Seq[Long]]
}

trait WithTableQuery[T <: BaseTable[A], A <: BaseEntity] {
  val tableQuery: TableQuery[T]
}

class BaseDaoImpl[T <: BaseTable[A], A <: BaseEntity](val tableQuery: TableQuery[T])
    (implicit val db: JdbcProfile#Backend#Database, implicit val profile: JdbcProfile)
    extends BaseDao[T, A] with BaseDBIODao[T, A] with Profile with DbModule with WithTableQuery[T, A] {

  import profile.api._

  override def insert(row: A): Future[Long] = db.run(insertQ(row))

  override def insert(rows: Seq[A]): Future[Seq[Long]] = db.run(insertQ(rows))

  override def insertQ(row: A): DBIO[Long] = insertQ(Seq(row)).map(_.head)

  override def insertQ(rows: Seq[A]): DBIO[Seq[Long]] = {
    tableQuery returning tableQuery.map(_.id) ++= rows
  }

  override def update(row: A): Future[Int] = db.run(
    tableQuery.filter(_.id === row.id).update(row)
  )

  override def update(rows: Seq[A]): Future[Unit] = db.run(
    DBIO.seq(rows.map(r => tableQuery.filter(_.id === r.id).update(r)): _*)
  )

  override def findById(id: Long): Future[Option[A]] = db.run(
    tableQuery.filter(_.id === id).result.headOption
  )

  override def findByFilter[C: CanBeQueryCondition](f: T => C): Future[Seq[A]] = db.run(
    tableQuery.withFilter(f).result
  )

  override def deleteById(id: Long): Future[Int] = deleteById(Seq(id))

  override def deleteById(ids: Seq[Long]): Future[Int] = db.run(
    tableQuery.filter(_.id.inSet(ids)).delete
  )

  override def deleteByFilter[C: CanBeQueryCondition](f: T => C): Future[Int] = db.run(
    tableQuery.withFilter(f).delete
  )

  override def list: Future[Seq[A]] = db.run(tableQuery.result)
}