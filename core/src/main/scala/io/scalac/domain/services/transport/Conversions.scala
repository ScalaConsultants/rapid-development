package io.scalac.domain.services.transport

import java.util.UUID

import io.scalac.domain.entities.Note
import org.joda.time.DateTime

object Conversions {

  def toOutgoingNote(note: Note) =
    OutgoingNote(
      id = note.id.get,
      creator = note.creator,
      note = note.note,
      recentEdit = note.updatedAt
    )

  def fromIncomingNote(incomingNote: IncomingNote) = {
    val now = DateTime.now()
    Note(Some(UUID.randomUUID()), incomingNote.creator, incomingNote.note, now, now, None)
  }
}
