package io.scalac.common.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator

trait BearerTokenEnv extends Env {

  type I = AuthUser
  type A = BearerTokenAuthenticator
}
