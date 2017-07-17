package io.scalac.domain.entities

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.repository.Repository
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.basic.DatabaseConfig

import io.scalac.common.core.{TokenId, UserId}
import io.scalac.common.db.PostgresJdbcProfile

final case class Token(
  override val id: Option[TokenId],
  userId: UserId,
  expiry: DateTime,
  createdAt: DateTime
) extends Entity[Token, TokenId] {
  override def withId(id: TokenId): Token = this.copy(id = Some(id))
}

class TokensSlickPostgresRepository (
  val dbConfig: DatabaseConfig[PostgresJdbcProfile]
) extends Repository[Token, TokenId](dbConfig.profile) with CommonMappers {

  import dbConfig.profile.api._
  override type TableType = TokensTable
  override def pkType = implicitly[BaseTypedType[TokenId]]
  override def tableQuery = TableQuery[TokensTable]

  class TokensTable(tag: slick.lifted.Tag) extends Table[Token](tag, "tokens") with Keyed[TokenId] {
    override def id = column[TokenId]("id", O.PrimaryKey)
    def userId = column[UserId]("user_id", O.Unique)
    def expiry = column[DateTime]("expiry", O.SqlType("timestamptz"))
    def createdAt = column[DateTime]("created_at", O.SqlType("timestamptz"))

    def * = (id.?, userId, expiry, createdAt) <> ((Token.apply _).tupled, Token.unapply)
  }
}
