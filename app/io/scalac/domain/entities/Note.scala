package io.scalac.domain.entities

import java.util.UUID

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.repository.Repository
import org.joda.time.DateTime
import slick.ast.BaseTypedType

final case class Note(
  override val id: Option[UUID],
  creator: String,
  note: String,
  createdAt: DateTime,
  updatedAt: DateTime,
  version: Int
) extends Entity[Note, UUID] {
  override def withId(id: UUID): Note = this.copy(id = Some(id))
}

class NotesRepository() extends Repository[Note, UUID](PostgresJdbcProfile) {

  import PostgresJdbcProfile.api._
  override type TableType = Notes
  override def pkType = implicitly[BaseTypedType[UUID]]
  override def tableQuery = TableQuery[Notes]

  class Notes(tag: slick.lifted.Tag) extends Table[Note](tag, "NOTE") with Keyed[UUID] {
    def id = column[UUID]("ID", O.PrimaryKey)
    def creator = column[String]("CREATOR", O.SqlType("VARCHAR(100)"))
    def note = column[String]("NOTE", O.SqlType("TEXT"))
    def createdAt = column[DateTime]("CREATED_AT")
    def updatedAt = column[DateTime]("UPDATED_AT")
    def version = column[Int]("VERSION")

    def * = (id.?, creator, note, createdAt, updatedAt, version) <> ((Note.apply _).tupled, Note.unapply)
  }
}
