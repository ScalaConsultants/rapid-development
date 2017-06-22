package io.scalac.common.services

final case class HealthCheckResponse(
  success: Boolean,
  message: String,
  environment: String,
  hostName: String,
  buildTimestamp: Option[String],
  gitBranch: Option[String],
  gitCommit: Option[String],
  project: Option[String],
  build: Option[String],
  instigator: Option[String],
  externalComponents: Seq[ExternalHealthCheckResponse] = Seq()
)
