package io.scalac.common.services

import monix.eval.Task

trait ExternalHealthCheck {

  def apply(): Task[ExternalHealthCheckResponse]
}

class ExternalHealthChecks (
  databaseHealthCheck: DatabaseHealthCheck
) {
  val services: Seq[ExternalHealthCheck] = Seq(databaseHealthCheck)
}
