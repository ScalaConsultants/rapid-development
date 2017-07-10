package io.scalac.domain.mock

import java.util.UUID

import io.scalac.common.entities.Pagination
import io.scalac.common.services.DatabaseResponse
import io.scalac.domain.dao.NotesDao
import io.scalac.domain.entities.Note

class NotesDaoMock extends NotesDao {

  override def findAll(): DatabaseResponse[Seq[Note]] = ???

  override def listAll(pagination: Pagination): DatabaseResponse[Seq[Note]] = ???

  override def find(noteId: UUID): DatabaseResponse[Option[Note]] = ???

  override def create(note: Note): DatabaseResponse[UUID] = ???

  override def update(note: Note): DatabaseResponse[Note] = ???
}
