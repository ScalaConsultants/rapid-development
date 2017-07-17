package io.scalac.domain.entities

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.repository.VersionedRepository
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.basic.DatabaseConfig

import io.scalac.common.core.UserId
import io.scalac.common.db.PostgresJdbcProfile

final case class User(
  override val id: Option[UserId],
  firstName: String,
  lastName: String,
  email: String,
  avatarURL: Option[String],
//  activated = false ??
  createdAt: DateTime,
  updatedAt: DateTime,
  override val version: Option[Int]
) extends VersionedEntity[User, UserId, Int] {

  override def withId(id: UserId): User = this.copy(id = Some(id))
  override def withVersion(version: Int): User = this.copy(version = Some(version))
}

class UsersSlickPostgresRepository (
  val dbConfig: DatabaseConfig[PostgresJdbcProfile]
) extends VersionedRepository[User, UserId, Int](dbConfig.profile) with CommonMappers {

  import dbConfig.profile.api._
  override type TableType = UsersTable
  override val versionType = implicitly[BaseTypedType[Int]]
  override def pkType = implicitly[BaseTypedType[UserId]]
  override def tableQuery = TableQuery[UsersTable]

  class UsersTable(tag: slick.lifted.Tag) extends Table[User](tag, "users") with Versioned[UserId, Int] {
    override def id = column[UserId]("id", O.PrimaryKey)
    def firstName = column[String]("first_name", O.SqlType("VARCHAR(50)"))
    def lastName = column[String]("last_name", O.SqlType("VARCHAR(50)"))
    def email = column[String]("email", O.SqlType("VARCHAR(50)"))
    def avatarUrl = column[String]("avatar_url", O.SqlType("VARCHAR(200)"))
    def createdAt = column[DateTime]("created_at", O.SqlType("timestamptz"))
    def updatedAt = column[DateTime]("updated_at", O.SqlType("timestamptz"))
    override def version = column[Int]("version")

    def * = (id.?, firstName, lastName, email, avatarUrl.?, createdAt, updatedAt, version.?) <> ((
      User.apply _).tupled, User.unapply)
  }
}
