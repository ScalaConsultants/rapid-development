package io.scalac.services.auth

import java.util.UUID

import io.scalac.common.core.{AuthToken, TokenId, UserId}
import io.scalac.common.logger.Logging
import io.scalac.common.services._
import io.scalac.domain.dao.AuthTokenDao

trait AuthTokenService {

  def create: Service[UserId, AuthToken, AuthError]
  def validate: Service[TokenId, Boolean, AuthError]
  def clean: Service[Unit, Seq[AuthToken], AuthError]
}

class DefaultAuthTokenService(
  authTokenDao: AuthTokenDao,
  clock: AppClock
) extends AuthTokenService with Logging {

  override def create: Service[UserId, AuthToken, AuthError] =
    Service("io.scalac.services.auth.DefaultAuthTokenService.create") { req => _ =>
      //TODO get time shift from config

      val authToken = AuthToken(
        token = TokenId(UUID.randomUUID()),
        userId = req,
        expiry = clock.now.plusDays(1)
      )
      authTokenDao.save(authToken).toAuthResponse
    }

  override def validate: Service[TokenId, Boolean, AuthError] = ???

  override def clean: Service[Unit, Seq[AuthToken], AuthError] = ???
}
