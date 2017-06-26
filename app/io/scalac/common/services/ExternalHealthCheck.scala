package io.scalac.common.services

import com.google.inject.{Inject, Singleton}
import monix.eval.Task

trait ExternalHealthCheck {

  def apply(): Task[ExternalHealthCheckResponse]
}

@Singleton
class ExternalHealthChecks @Inject() (
  databaseHealthCheck: DatabaseHealthCheck
) {
  val services: Seq[ExternalHealthCheck] = Seq(databaseHealthCheck)
}
