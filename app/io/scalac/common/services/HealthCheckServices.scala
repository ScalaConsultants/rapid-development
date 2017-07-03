package io.scalac.common.services

trait HealthCheckServices {
  val healthCheck: Service[HealthCheckRequest, HealthCheckResponse, ServiceError]
}
