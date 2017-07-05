package io.scalac.common.services

import monix.eval.Task

trait ExternalHealthCheck {
  def apply(): Task[ExternalHealthCheckResponse]
}
