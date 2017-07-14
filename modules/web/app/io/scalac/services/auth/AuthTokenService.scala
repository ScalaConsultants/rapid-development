package io.scalac.services.auth

import java.util.UUID

import io.scalac.common.services._

trait AuthTokenService {

  def create: Service[UUID, String, ServiceError]
}
