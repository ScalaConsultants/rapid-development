package io.scalac.controllers.mock

import java.util.UUID

import io.scalac.common.entities.Pagination
import io.scalac.common.services.{Service, ServiceError, ServiceResponse}
import io.scalac.domain.services.MerchantsService
import io.scalac.domain.services.transport.{IncomingNote, OutgoingMerchant, UpdateNote}

// TODO: decide if should stay in module web or core
class NotesServiceMock extends MerchantsService {
  override def findAll(): ServiceResponse[Seq[OutgoingMerchant]] = ???
  override def list: Service[Pagination, Seq[OutgoingMerchant], ServiceError] = ???
  override def find: Service[UUID, Option[OutgoingMerchant], ServiceError] = ???
  override def create: Service[IncomingNote, UUID, ServiceError] = ???
  override def update: Service[UpdateNote, OutgoingMerchant, ServiceError] = ???
}
