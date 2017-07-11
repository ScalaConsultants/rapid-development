package io.scalac.common.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

final case class User(
  id: UUID,
  loginInfo: LoginInfo, //TODO separate to allow users have multiple authorizations
  firstName: String,
  lastName: String,
  email: String
//  avatarURL = None,
//  activated = false
) extends Identity
