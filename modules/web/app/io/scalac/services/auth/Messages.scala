package io.scalac.services.auth

import play.api.mvc.RequestHeader

import io.scalac.controllers.auth.{IncomingSignIn, IncomingSignUp}

final case class SingUpRequest(
  incomingSignUp: IncomingSignUp,
  requestHeader: RequestHeader
)

final case class SignInRequest(
  incomingSignIn: IncomingSignIn,
  requestHeader: RequestHeader
)
