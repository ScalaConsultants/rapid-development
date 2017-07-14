package io.scalac.domain.dao

import java.util.UUID

import io.scalac.common.db.DBExecutor
import io.scalac.common.services.DatabaseResponse
import io.scalac.domain.entities.{User, UsersSlickPostgresRepository}

trait UsersDao {
  
  def find(userId: UUID): DatabaseResponse[Option[User]]
  def create(user: User): DatabaseResponse[UUID]
  def update(user: User): DatabaseResponse[User]
}

class SlickUsersDao (
  usersRepo: UsersSlickPostgresRepository,
  dbExecutor: DBExecutor
) extends UsersDao {

  import dbExecutor._
  implicit val ec = dbExecutor.scheduler

  override def find(userId: UUID): DatabaseResponse[Option[User]] =
    usersRepo.findOne(userId)

  override def create(user: User): DatabaseResponse[UUID] =
    usersRepo.save(user).map(_.id.get)

  override def update(user: User): DatabaseResponse[User] =
    usersRepo.update(user)
}
