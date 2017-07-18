package io.scalac.services.auth

import java.util.UUID

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import monix.execution.Scheduler

import io.scalac.common.auth.AuthUser
import io.scalac.common.core.{AuthenticationProviderId, Correlation, UserId}
import io.scalac.common.logger.Logging
import io.scalac.common.services._
import io.scalac.common.syntax._
import io.scalac.domain.dao.AuthUsersDao
import io.scalac.domain.entities.AuthenticationProvider

trait AuthUserService extends IdentityService[AuthUser] {

  def store: Service[AuthUser, UserId, ServiceError]
}

class DefaultAuthUsersService(
  authUsersDao: AuthUsersDao,
  clock: AppClock
)(implicit scheduler: Scheduler) extends AuthUserService with Logging {

  override def retrieve(loginInfo: LoginInfo): Future[Option[AuthUser]] = {
    implicit val c = Correlation.withNotRequired
    authUsersDao.findByAuthProvider(loginInfo.providerID, loginInfo.providerKey).tmap { maybeUser =>
      maybeUser.map(AuthUser(loginInfo, _))
    }.runAsync.flatMap(_.fold(
      err => {
        logger.error("A DB error occurred when tried to find user by authentication provider data: " + err.toString)
        Future.failed[Option[AuthUser]](
          new IdentityNotFoundException("A DB error occurred when tried to find user by authentication provider data."))
      },
      maybeAuthUser => Future.successful(maybeAuthUser)
    ))
  }

  override def store: Service[AuthUser, UserId, ServiceError] =
    Service("io.scalac.services.auth.DefaultAuthUsersService.store") { req =>
      _ =>

        val now = clock.now
        val authorizationProvider = AuthenticationProvider(
          id = Some(AuthenticationProviderId(UUID.randomUUID())),
          providerId = req.loginInfo.providerID,
          providerKey = req.loginInfo.providerKey,
          createdAt = now,
          updatedAt = now,
          version = Some(1)
        )
        authUsersDao.create(req.user, authorizationProvider).toServiceResponse
    }

}
