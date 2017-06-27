package io.scalac.controllers

import play.api.libs.json.Json

import io.scalac.common.play.PaginatedResponse
import io.scalac.domain.entities.Note

object Serializers {

  import io.scalac.common.play.serializers.Serializers._

  implicit val noteFormat = Json.format[Note]
  implicit val paginatedResponseNoteFormat = Json.format[PaginatedResponse[Note]]
}
