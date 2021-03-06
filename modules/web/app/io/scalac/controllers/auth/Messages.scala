package io.scalac.controllers.auth

final case class IncomingSignUp(
  firstName: String,
  lastName: String,
  email: String,
  password: String
)

final case class IncomingSignIn(
  email: String,
  password: String,
  rememberMe: Boolean
)
