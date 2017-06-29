package io.scalac.domain.dao

import java.util.UUID

import com.google.inject.{Inject, Singleton}

import io.scalac.common.play.Pagination
import io.scalac.common.services.DatabaseResponse
import io.scalac.domain.entities.{Note, NotesSlickPostgresRepository}

trait NotesDao {

//  def findAll: Service[Unit, Seq[Note], DatabaseError] //TODO decide
  def findAll(): DatabaseResponse[Seq[Note]]
  def listAll(pagination: Pagination): DatabaseResponse[Seq[Note]]
  def find(noteId: UUID): DatabaseResponse[Option[Note]]
  def create(note: Note): DatabaseResponse[UUID]
  def update(note: Note): DatabaseResponse[Note]
}

@Singleton
class SlickNotesDao @Inject() (
  notesRepo: NotesSlickPostgresRepository,
  dBImplicits: DBImplicits
) extends NotesDao {

  import dBImplicits._

  override def findAll(): DatabaseResponse[Seq[Note]] =
    notesRepo.findAll()

  override def listAll(pagination: Pagination): DatabaseResponse[Seq[Note]] =
    notesRepo.listAll(pagination)

  override def find(noteId: UUID): DatabaseResponse[Option[Note]] =
    notesRepo.findOne(noteId)

  override def create(note: Note): DatabaseResponse[UUID] =
    notesRepo.save(note).map(_.id.get)

  override def update(note: Note): DatabaseResponse[Note] =
    notesRepo.update(note)
}

