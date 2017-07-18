package io.scalac.domain.entities

import java.util.UUID

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.repository.Repository
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.basic.DatabaseConfig

import io.scalac.common.core.AuthenticationProviderId
import io.scalac.common.db.PostgresJdbcProfile

final case class PasswordInformation(
  override val id: Option[UUID],
  authenticationProviderId: AuthenticationProviderId,
  hasher: String,
  password: String,
  salt: Option[String],
  createdAt: DateTime
) extends Entity[PasswordInformation, UUID] {
  override def withId(id: UUID): PasswordInformation = this.copy(id = Some(id))
}

class PasswordInformationSlickPostgresRepository (
  val dbConfig: DatabaseConfig[PostgresJdbcProfile]
) extends Repository[PasswordInformation, UUID](dbConfig.profile) with CommonMappers {

  import dbConfig.profile.api._
  override type TableType = PasswordInformationTable
  override def pkType = implicitly[BaseTypedType[UUID]]
  override def tableQuery = TableQuery[PasswordInformationTable]

  class PasswordInformationTable(tag: slick.lifted.Tag)
    extends Table[PasswordInformation](tag, "password_information") with Keyed[UUID] {

    override def id = column[UUID]("id", O.PrimaryKey)
    def authProviderId = column[AuthenticationProviderId]("auth_provider_id")
    def hasher = column[String]("hasher", O.SqlType("VARCHAR(200)"))
    def password = column[String]("password", O.SqlType("VARCHAR(200)"))
    def salt = column[String]("salt", O.SqlType("VARCHAR(200)"))
    def createdAt = column[DateTime]("created_at", O.SqlType("timestamptz"))

    def * = (id.?, authProviderId, hasher, password, salt.?, createdAt) <> ((
      PasswordInformation.apply _).tupled, PasswordInformation.unapply)
  }

  val findByAuthProviderId = Compiled { (providerId: Rep[AuthenticationProviderId]) =>
    tableQuery.filter(t => t.authProviderId === providerId)
  }
}
