package io.scalac.common.services

final case class ExternalHealthCheckResponse(
  success: Boolean,
  responseTime: Long,
  component: String,
  details: Map[String, String] = Map(),
  error: Option[String] = None
)
