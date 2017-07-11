package io.scalac.services

import java.util.UUID

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService

import io.scalac.common.auth.User
import io.scalac.common.services._

trait UserService extends IdentityService[User] {

  def save: Service[User, UUID, ServiceError]
}

class DefaultUserService extends UserService {

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = ???

  override def save: Service[User, UUID, ServiceError] = ???
}
