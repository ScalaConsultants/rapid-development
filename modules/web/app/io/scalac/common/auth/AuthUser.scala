package io.scalac.common.auth

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import io.scalac.domain.entities.User

final case class AuthUser(
  loginInfo: LoginInfo,
  user: User
) extends Identity
