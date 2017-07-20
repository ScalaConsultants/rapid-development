package io.scalac.domain.entities

import java.util.UUID

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.repository.VersionedRepository
import io.scalac.common.db.PostgresJdbcProfile
import io.scalac.common.entities.Pagination
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.basic.DatabaseConfig

final case class Note(
  override val id: Option[UUID],
  creator: String,
  note: String,
  createdAt: DateTime,
  updatedAt: DateTime,
  override val version: Option[Int]
) extends VersionedEntity[Note, UUID, Int] {

  override def withId(id: UUID): Note = this.copy(id = Some(id))
  override def withVersion(version: Int): Note = this.copy(version = Some(version))

  def update(creator: String, note: String) =
    this.copy(creator = creator, note = note, updatedAt = DateTime.now())
}

class NotesSlickPostgresRepository (
  val dbConfig: DatabaseConfig[PostgresJdbcProfile]
) extends VersionedRepository[Note, UUID, Int](dbConfig.profile) {

  import dbConfig.profile.api._
  override type TableType = Notes
  override val versionType = implicitly[BaseTypedType[Int]]
  override def pkType = implicitly[BaseTypedType[UUID]]
  override def tableQuery = TableQuery[Notes]

  class Notes(tag: slick.lifted.Tag) extends Table[Note](tag, "notes") with Versioned[UUID, Int] {
    override def id = column[UUID]("id", O.PrimaryKey)
    def creator = column[String]("creator", O.SqlType("VARCHAR(100)"))
    def note = column[String]("note", O.SqlType("TEXT"))
    def createdAt = column[DateTime]("created_at", O.SqlType("timestamptz"))
    def updatedAt = column[DateTime]("updated_at", O.SqlType("timestamptz"))
    override def version = column[Int]("version")

    def * = (id.?, creator, note, createdAt, updatedAt, version.?) <> ((
      Note.apply _).tupled, Note.unapply)
  }

  def listAll(pagination: Pagination): DBIO[Seq[Note]] = {
    Compiled(tableQuery.drop(pagination.offset).take(pagination.limit)).result
  }
}
