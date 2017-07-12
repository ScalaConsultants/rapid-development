package io.scalac.controllers

import io.scalac.common.entities.PaginatedResponse
import play.api.libs.json.Json

import io.scalac.common.play.serializers.Serializers._
import io.scalac.domain.services.merchants.OutgoingMerchant
import io.scalac.domain.services.transport.{IncomingNote, OutgoingNote}

object Serializers {

  implicit val noteFormat = Json.format[OutgoingNote]
  implicit val newNoteFormat = Json.format[IncomingNote]
  implicit val paginatedResponseNoteFormat = Json.format[PaginatedResponse[OutgoingNote]]

  implicit val outgoingMerchantFormat = Json.format[OutgoingMerchant]
  implicit val paginatedResponseOutgoingMerchantsFormat = Json.format[PaginatedResponse[OutgoingMerchant]]
}
