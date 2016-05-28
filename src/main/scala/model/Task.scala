package model

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

case class Task(
  id: Long,
  title: String,
  description: Option[String] = None,
  deadline: LocalDate,
  isDone: Boolean,
  createdAt: DateTime,
  updatedAt: DateTime
)

object Task extends SkinnyCRUDMapper[Task] with TimestampsFeature[Task] {
  override lazy val tableName = "tasks"
  override lazy val defaultAlias = createAlias("t")

  /*
   * If you're familiar with ScalikeJDBC/Skinny ORM, using #autoConstruct makes your mapper simpler.
   * (e.g.)
   * override def extract(rs: WrappedResultSet, rn: ResultName[Task]) = autoConstruct(rs, rn)
   *
   * Be aware of excluding associations like this:
   * (e.g.)
   * case class Member(id: Long, companyId: Long, company: Option[Company] = None)
   * object Member extends SkinnyCRUDMapper[Member] {
   *   override def extract(rs: WrappedResultSet, rn: ResultName[Member]) =
   *     autoConstruct(rs, rn, "company") // "company" will be skipped
   * }
   */
  override def extract(rs: WrappedResultSet, rn: ResultName[Task]): Task = new Task(
    id = rs.get(rn.id),
    title = rs.get(rn.title),
    description = rs.get(rn.description),
    deadline = rs.get(rn.deadline),
    isDone = rs.get(rn.isDone),
    createdAt = rs.get(rn.createdAt),
    updatedAt = rs.get(rn.updatedAt)
  )
}
