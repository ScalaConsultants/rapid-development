package io.scalac.services

import java.util.UUID

import cats.syntax.either._
import com.google.inject.{Inject, Singleton}
import monix.cats.monixToCatsMonad
import org.joda.time.DateTime

import io.scalac.common.logger.Logging
import io.scalac.common.play.Pagination
import io.scalac.common.services._
import io.scalac.common.syntax._
import io.scalac.domain.dao.NotesDao
import io.scalac.domain.entities.Note

trait NotesService {

  def findAll(): Response[Seq[OutgoingNote]] //TODO prepare Service without request...
  def list: Service[Pagination, Seq[OutgoingNote], ServiceError]
  def find: Service[UUID, Option[OutgoingNote], ServiceError]
  def create: Service[IncomingNote, UUID, ServiceError]
  def update: Service[UpdateNote, OutgoingNote, ServiceError]
}

@Singleton
class DefaultNotesService @Inject()(
  notesDao: NotesDao,
  implicit val profiler: ServiceProfiler
) extends NotesService with Logging {

  override def findAll(): Response[Seq[OutgoingNote]] = {
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
        //TODO add some cats validation
        val now = DateTime.now()
        val note = Note(Some(UUID.randomUUID()), req.creator, req.note, now, now, None)
        notesDao.create(note).toServiceResponse
    }

  override def update: Service[UpdateNote, OutgoingNote, ServiceError] =
    Service("io.scalac.services.DefaultNotesService.update") { req =>
      implicit serviceContext =>

        val findExistingNote: DBResponse[Note] = notesDao.find(req.id).tflatMap { optNote =>
          optNote.fold(ResourceNotFound("Cannot update non-existent element").asLeft[Note])(_.asRight)
        }

        def performUpdate(existingNote: Note) = {
          //TODO add some cats validation
          val noteToUpdate = existingNote.update(
            creator = req.incomingNote.creator,
            note = req.incomingNote.note)
          notesDao.update(noteToUpdate)
        }

        val op = for {
          existingNote <- findExistingNote.eitherT
          updatedNote <- performUpdate(existingNote).eitherT
        } yield Conversions.toOutgoingNote(updatedNote)

        op.value.toServiceResponse
    }
}
