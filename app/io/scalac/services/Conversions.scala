package io.scalac.services

import io.scalac.domain.entities.Note

object Conversions {

  def toOutgoingNote(note: Note) =
    OutgoingNote(
      id = note.id.get,
      creator = note.creator,
      note = note.note,
      recentEdit = note.updatedAt
    )
}
