package io.scalac.services

import java.util.UUID

import org.joda.time.DateTime

final case class IncomingNote(
  creator: String,
  note: String
)

final case class OutgoingNote(
  id: UUID,
  creator: String,
  note: String,
  recentEdit: DateTime
)

final case class UpdateNote(
  id: UUID,
  incomingNote: IncomingNote
)
