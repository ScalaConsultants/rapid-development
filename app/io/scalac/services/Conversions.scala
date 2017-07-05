package io.scalac.services

import java.util.UUID

import org.joda.time.DateTime

import io.scalac.domain.entities.Note

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
