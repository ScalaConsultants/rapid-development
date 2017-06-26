package io.scalac.domain.entities

import java.util.UUID

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.repository.VersionedRepository
import com.google.inject.{Inject, Singleton}
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.basic.DatabaseConfig

import io.scalac.domain.PostgresJdbcProfile

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
}

//TODO or JdbcProfile and import PostgresJdbcProfile.api._ below? Safe?
@Singleton
class NotesSlickPostgresRepository @Inject() (
//  val dbProfile: PostgresJdbcProfile
  val dbConfig: DatabaseConfig[PostgresJdbcProfile]
) extends VersionedRepository[Note, UUID, Int](dbConfig.profile) {

//  import dbProfile.api._
  import dbConfig.profile.api._
  override type TableType = Notes
  override val versionType = implicitly[BaseTypedType[Int]]
  override def pkType = implicitly[BaseTypedType[UUID]]
  override def tableQuery = TableQuery[Notes]

  class Notes(tag: slick.lifted.Tag) extends Table[Note](tag, "NOTE") with Versioned[UUID, Int] {
    override def id = column[UUID]("ID", O.PrimaryKey)
    def creator = column[String]("CREATOR", O.SqlType("VARCHAR(100)"))
    def note = column[String]("NOTE", O.SqlType("TEXT"))
    def createdAt = column[DateTime]("CREATED_AT")
    def updatedAt = column[DateTime]("UPDATED_AT")
    override def version = column[Int]("VERSION")

    def * = (id.?, creator, note, createdAt, updatedAt, version.?) <> ((
      Note.apply _).tupled, Note.unapply)
  }
}
