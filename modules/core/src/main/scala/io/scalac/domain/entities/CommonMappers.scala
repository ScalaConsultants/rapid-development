package io.scalac.domain.entities

import java.util.UUID

import io.scalac.common.core.{AuthenticationProviderId, TokenId, UserId}

trait CommonMappers {

  import io.scalac.common.db.PostgresJdbcProfile.api._

  implicit val tokenIdMapper: BaseColumnType[TokenId] =
    MappedColumnType.base[TokenId, UUID](_.id, TokenId)

  implicit val userIdMapper: BaseColumnType[UserId] =
    MappedColumnType.base[UserId, UUID](_.id, UserId)

  implicit val authenticationProviderIdMapper: BaseColumnType[AuthenticationProviderId] =
    MappedColumnType.base[AuthenticationProviderId, UUID](_.id, AuthenticationProviderId)
}
