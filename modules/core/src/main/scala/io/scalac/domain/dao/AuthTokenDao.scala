package io.scalac.domain.dao

import org.joda.time.DateTime

import io.scalac.common.core.{AuthToken, TokenId}
import io.scalac.common.db.DBExecutor
import io.scalac.common.services.DatabaseResponse
import io.scalac.domain.entities.{CommonMappers, TokensSlickPostgresRepository, UsersSlickPostgresRepository}

trait AuthTokenDao {

  /**
    * Finds a token by its ID.
    *
    * @param id The unique token ID.
    * @return The found token or None if no token for the given ID could be found.
    */
  def find(id: TokenId): DatabaseResponse[Option[AuthToken]]
  /**
    * Finds expired tokens.
    *
    * @param dateTime The current date time.
    */
  def findExpired(dateTime: DateTime): DatabaseResponse[Seq[AuthToken]]
  /**
    * Saves a token.
    *
    * @param token The token to save.
    * @return The saved token.
    */
  def save(token: AuthToken): DatabaseResponse[AuthToken]
  /**
    * Removes the token for the given ID.
    *
    * @param id The ID for which the token should be removed.
    * @return A future to wait for the process to be completed.
    */
  def remove(id: TokenId): DatabaseResponse[Unit]
}

class SlickAuthTokenDao(
  tokensRepo: TokensSlickPostgresRepository,
  usersRepo: UsersSlickPostgresRepository,
  dbExecutor: DBExecutor
) extends AuthTokenDao with CommonMappers {

  import dbExecutor._
  import dbExecutor.dbConfig.profile.api._
  implicit val ec = dbExecutor.scheduler

  override def find(id: TokenId): DatabaseResponse[Option[AuthToken]] = {
    val q = for {
      (t, u) <- tokensRepo.tableQuery join usersRepo.tableQuery on (_.userId === _.id) if t.id === id
    } yield (t, u)

    Compiled(q).result.headOption.map {
      case Some((token, user)) => Some(AuthToken(token.id.get, user.id.get, token.expiry))
      case None => None
    }
  }

  override def findExpired(dateTime: DateTime): DatabaseResponse[Seq[AuthToken]] = ???

  override def save(token: AuthToken): DatabaseResponse[AuthToken] = ???

  override def remove(id: TokenId): DatabaseResponse[Unit] = ???
}
