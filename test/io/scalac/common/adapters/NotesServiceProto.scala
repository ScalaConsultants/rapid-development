package io.scalac.common.adapters

import java.util.UUID

import io.scalac.common.play.Pagination
import io.scalac.common.services.{Service, ServiceError, ServiceResponse}
import io.scalac.services.{IncomingNote, NotesService, OutgoingNote, UpdateNote}

class NotesServiceProto extends NotesService {
  override def findAll(): ServiceResponse[Seq[OutgoingNote]] = ???
  override def list: Service[Pagination, Seq[OutgoingNote], ServiceError] = ???
  override def find: Service[UUID, Option[OutgoingNote], ServiceError] = ???
  override def create: Service[IncomingNote, UUID, ServiceError] = ???
  override def update: Service[UpdateNote, OutgoingNote, ServiceError] = ???
}
