package io.scalac.services.auth

import java.util.UUID

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService

import io.scalac.common.auth.AuthUser
import io.scalac.common.services._
import io.scalac.domain.dao.UsersDao

trait AuthUserService extends IdentityService[AuthUser] {

  def save: Service[AuthUser, UUID, ServiceError]
}

class DefaultAuthUsersService(
  usersDao: UsersDao
) extends AuthUserService {


  override def retrieve(loginInfo: LoginInfo): Future[Option[AuthUser]] = ???

  override def save: Service[AuthUser, UUID, ServiceError] =  {
    ???
  }

}
