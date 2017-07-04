package io.scalac.services

import java.util.UUID

import cats.syntax.either._
import io.scalac.common.logger.Logging
import io.scalac.common.play.Pagination
import io.scalac.common.services._
import io.scalac.common.syntax._
import io.scalac.domain.dao.NotesDao
import io.scalac.domain.entities.Note
import io.scalac.services.ValidatedUtils._
import monix.cats.monixToCatsMonad
import org.joda.time.DateTime

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
    Service("io.scalac.services.DefaultNotesService.list") { req =>
      implicit serviceContext =>
        notesDao.listAll(req).tmap(_.map(Conversions.toOutgoingNote)).toServiceResponse
    }

  override def find: Service[UUID, Option[OutgoingNote], ServiceError] =
    Service("io.scalac.services.DefaultNotesService.find") { req =>
      implicit serviceContext =>
        notesDao.find(req).tmap(_.map(Conversions.toOutgoingNote)).toServiceResponse
    }

  override def create: Service[IncomingNote, UUID, ServiceError] =
    Service("io.scalac.services.DefaultNotesService.create") { req =>
      implicit serviceContext =>

        val now = DateTime.now()
        (for {
          _       <- req.validate().asDatabaseResponse.eitherT
          note    = Note(Some(UUID.randomUUID()), req.creator, req.note, now, now, None)
          newUUID <- notesDao.create(note).eitherT
        } yield newUUID).value.toServiceResponse
    }

  override def update: Service[UpdateNote, OutgoingNote, ServiceError] =
    Service("io.scalac.services.DefaultNotesService.update") { req =>
      implicit serviceContext =>

        def findExistingNote: DatabaseResponse[Note] = notesDao.find(req.id).tflatMap { optNote =>
          optNote.fold(ResourceNotFound("Cannot update non-existent element").asLeft[Note])(_.asRight)
        }

        def performUpdate(existingNote: Note) = {
          val noteToUpdate = existingNote.update(
            creator = req.incomingNote.creator,
            note = req.incomingNote.note)
          notesDao.update(noteToUpdate)
        }

        val op = for {
          _            <- req.incomingNote.validate().asDatabaseResponse.eitherT
          existingNote <- findExistingNote.eitherT
          updatedNote  <- performUpdate(existingNote).eitherT
        } yield Conversions.toOutgoingNote(updatedNote)

        op.value.toServiceResponse
    }
}
