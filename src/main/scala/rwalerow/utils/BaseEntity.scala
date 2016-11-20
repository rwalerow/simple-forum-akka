package rwalerow.utils

import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import slick.lifted.{CanBeQueryCondition, TableQuery}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * source: https://github.com/cdiniz/slick-akka-http/tree/master/src/main/scala/persistence/entities
  */
trait BaseEntity {
  val id: Long
}

abstract class BaseTable[T](tag: Tag, name: String) extends Table[T](tag, name){
  def id = column[Long]("id", O.PrimaryKey)
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
}

trait WithTableQuery[T <: BaseTable[A], A <: BaseEntity] {
  val tableQuery: TableQuery[T]
}

class BaseDaoImpl[T <: BaseTable[A], A <: BaseEntity](val tableQuery: TableQuery[T])
    (implicit val db: JdbcProfile#Backend#Database, implicit val profile: JdbcProfile)
    extends BaseDao[T, A] with Profile with DbModule with WithTableQuery[T, A] {

  import profile.api._

  override def insert(row: A): Future[Long] = insert(Seq(row)).map(_.head)

  override def insert(rows: Seq[A]): Future[Seq[Long]] = db.run(
    tableQuery returning tableQuery.map(_.id) ++= rows
  )

  override def update(row: A): Future[Int] = db.run(
    tableQuery.filter(_.id === row.id).update(row)
  )

  override def update(rows: Seq[A]): Future[Unit] = db.run(
    DBIO.seq(rows.map(r => tableQuery.filter(_.id === r.id).update(r)): _*)
  )

  override def findById(id: Long): Future[Option[A]] = db.run(
    tableQuery.filter(_.id === id).result.headOption
  )

  override def findByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Seq[A]] = db.run(
    tableQuery.withFilter(f).result
  )

  override def deleteById(id: Long): Future[Int] = deleteById(Seq(id))

  override def deleteById(ids: Seq[Long]): Future[Int] = db.run(
    tableQuery.filter(_.id.inSet(ids)).delete
  )

  override def deleteByFilter[C: CanBeQueryCondition](f: T => C): Future[Int] = db.run(
    tableQuery.withFilter(f).delete
  )
}