package io.scalac.controllers

import play.api.libs.json.Json

import io.scalac.common.play.PaginatedResponse
import io.scalac.common.play.serializers.Serializers._
import io.scalac.services.{OutgoingNote, IncomingNote}

object Serializers {

  implicit val noteFormat = Json.format[OutgoingNote]
  implicit val newNoteFormat = Json.format[IncomingNote]
  implicit val paginatedResponseNoteFormat = Json.format[PaginatedResponse[OutgoingNote]]
}
