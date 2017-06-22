package io.scalac.common.services

import com.google.inject.Singleton
import monix.eval.Task

trait ExternalHealthCheck {

  def apply(): Task[ExternalHealthCheckResponse]
}

@Singleton
final case class ExternalHealthChecks(
  services: Seq[ExternalHealthCheck]
)
