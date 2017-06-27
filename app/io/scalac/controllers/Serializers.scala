package io.scalac.controllers

import play.api.libs.json.Json

import io.scalac.domain.entities.Note

object Serializers {

  import io.scalac.common.play.serializers.Serializers.dateTimeFormat

  implicit val noteFormat = Json.format[Note]
}
