package io.scalac.domain.dao

import io.scalac.common.core.UserId
import io.scalac.common.db.DBExecutor
import io.scalac.common.services.DatabaseResponse
import io.scalac.domain.entities.{AuthenticationProvider, AuthenticationProvidersSlickPostgresRepository, User, UsersSlickPostgresRepository}

trait AuthUsersDao {

  def create(user: User, authenticationProvider: AuthenticationProvider): DatabaseResponse[UserId]
  def findByAuthProvider(providerId: String, providerKey: String): DatabaseResponse[Option[User]]
}

class SlickAuthUsersDao(
  usersRepo: UsersSlickPostgresRepository,
  authenticationProvidersRepository: AuthenticationProvidersSlickPostgresRepository,
  dbExecutor: DBExecutor
) extends AuthUsersDao {

  import dbExecutor._
  import usersRepo.dbConfig.profile.api._
  implicit val ec = dbExecutor.scheduler

  override def create(user: User, authenticationProvider: AuthenticationProvider): DatabaseResponse[UserId] = {

    val op = for {
      createdUser <- usersRepo.save(user)
      _           <- authenticationProvidersRepository.save(authenticationProvider)
    } yield createdUser.id.get

    executeTransactionally(op)
  }

  override def findByAuthProvider(providerId: String, providerKey: String): DatabaseResponse[Option[User]] = {
    val q = for {
      (auth, user) <- authenticationProvidersRepository.tableQuery join usersRepo.tableQuery on { case (auth, users) =>
        auth.providerKey === users.email
      } if user.email === providerKey && auth.providerId === providerId
    } yield user
    Compiled(q).result.headOption
  }
}
