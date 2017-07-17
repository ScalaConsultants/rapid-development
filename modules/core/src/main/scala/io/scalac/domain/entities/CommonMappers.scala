package io.scalac.domain.entities

import java.util.UUID

import io.scalac.common.core.{TokenId, UserId}

trait CommonMappers {

  import io.scalac.common.db.PostgresJdbcProfile.api._

  implicit val tokenIdMapper: BaseColumnType[TokenId] =
    MappedColumnType.base[TokenId, UUID](_.id, TokenId)

  implicit val userIdMapper: BaseColumnType[UserId] =
    MappedColumnType.base[UserId, UUID](_.id, UserId)
}
