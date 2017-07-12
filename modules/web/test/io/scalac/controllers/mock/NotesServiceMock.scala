package io.scalac.controllers.mock

import java.util.UUID

import io.scalac.common.entities.Pagination
import io.scalac.common.services.{Service, ServiceError, ServiceResponse}
import io.scalac.domain.services.NotesService
import io.scalac.domain.services.transport.{IncomingNote, OutgoingNote, UpdateNote}

// TODO: decide if should stay in module web or core
class NotesServiceMock extends NotesService {
  override def findAll(): ServiceResponse[Seq[OutgoingNote]] = ???
  override def list: Service[Pagination, Seq[OutgoingNote], ServiceError] = ???
  override def find: Service[UUID, Option[OutgoingNote], ServiceError] = ???
  override def create: Service[IncomingNote, UUID, ServiceError] = ???
  override def update: Service[UpdateNote, OutgoingNote, ServiceError] = ???
}
