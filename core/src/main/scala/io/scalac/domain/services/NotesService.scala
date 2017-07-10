package io.scalac.domain.services

import java.util.UUID

import cats.syntax.either._
import io.scalac.common.entities.Pagination
import io.scalac.common.logger.Logging
import io.scalac.common.services._
import io.scalac.common.syntax._
import io.scalac.domain.dao.NotesDao
import io.scalac.domain.entities.Note
import io.scalac.domain.services.transport.{Conversions, IncomingNote, OutgoingNote, UpdateNote}
import monix.cats.monixToCatsMonad
import monix.eval.Task

trait NotesService {

  def findAll(): ServiceResponse[Seq[OutgoingNote]] //TODO prepare Service without request...
  def list: Service[Pagination, Seq[OutgoingNote], ServiceError]
  def find: Service[UUID, Option[OutgoingNote], ServiceError]
  def create: Service[IncomingNote, UUID, ServiceError]
  def update: Service[UpdateNote, OutgoingNote, ServiceError]
}

class DefaultNotesService (
    notesDao: NotesDao)(implicit val profiler: ServiceProfiler)
  extends NotesService with Logging {

  override def findAll(): ServiceResponse[Seq[OutgoingNote]] = {
    //TODO what to do with db message? Map to Service message or just pass through?
    notesDao.findAll().tmap(_.map(Conversions.toOutgoingNote)).toServiceResponse
  }

  override def list: Service[Pagination, Seq[OutgoingNote], ServiceError] =
    Service("io.scalac.services.DefaultNotesService.list") { req => _ =>
        notesDao.listAll(req).tmap(_.map(Conversions.toOutgoingNote)).toServiceResponse
    }

  override def find: Service[UUID, Option[OutgoingNote], ServiceError] =
    Service("io.scalac.services.DefaultNotesService.find") { req => _ =>
        notesDao.find(req).tmap(_.map(Conversions.toOutgoingNote)).toServiceResponse
    }

  override def create: Service[IncomingNote, UUID, ServiceError] =
    Service("io.scalac.services.DefaultNotesService.create") { req => _ =>
        req.validate().fold(
          invalid => Task.now(InvalidResource(invalid.toList).asLeft),
          valid => {
            val note = Conversions.fromIncomingNote(valid)
            notesDao.create(note).toServiceResponse
          }
        )
    }

  override def update: Service[UpdateNote, OutgoingNote, ServiceError] =
    Service("io.scalac.services.DefaultNotesService.update") { req => _ =>

        def findExistingNote: DatabaseResponse[Note] = notesDao.find(req.id).tflatMap { optNote =>
          optNote.fold(ResourceNotFound("Cannot update non-existent element").asLeft[Note])(_.asRight)
        }

        def performUpdate(incomingNote: IncomingNote, existingNote: Note) = {
          val noteToUpdate = existingNote.update(
            creator = incomingNote.creator,
            note = incomingNote.note)
          notesDao.update(noteToUpdate)
        }

        req.incomingNote.validate().fold(
          invalid => Task.now(InvalidResource(invalid.toList).asLeft),
          valid => {
            (for {
              existingNote <- findExistingNote.eitherT
              updatedNote  <- performUpdate(valid, existingNote).eitherT
            } yield Conversions.toOutgoingNote(updatedNote)).value.toServiceResponse
          }
        )
    }
}
