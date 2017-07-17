package io.scalac.services.auth

import io.scalac.common.core.{AuthToken, TokenId, UserId}
import io.scalac.common.logger.Logging
import io.scalac.common.services._
import io.scalac.domain.dao.AuthTokenDao

trait AuthTokenService {

  def create: Service[UserId, AuthToken, ServiceError]
  def validate: Service[TokenId, Boolean, ServiceError]
  def clean: Service[Unit, Seq[AuthToken], ServiceError]
}

class DefaultAuthTokenService(
  authTokenDao: AuthTokenDao,
  clock: AppClock
) extends AuthTokenService with Logging {

  override def create: Service[UserId, AuthToken, ServiceError] =
    Service("io.scalac.services.auth.DefaultAuthTokenService.create") { req => _ =>


      ???
    }

  override def validate: Service[TokenId, Boolean, ServiceError] = ???

  override def clean: Service[Unit, Seq[AuthToken], ServiceError] = ???
}
