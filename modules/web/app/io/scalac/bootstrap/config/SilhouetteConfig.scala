package io.scalac.bootstrap.config

import scala.concurrent.duration.FiniteDuration

final case class Authenticator(
  idleTimeout: FiniteDuration,
  expiry: FiniteDuration,
  rememberMe: RememberMe
)

final case class RememberMe(
  idleTimeout: FiniteDuration,
  expiry: FiniteDuration
)

final case class SilhouetteConfig(
  authenticator: Authenticator
)
