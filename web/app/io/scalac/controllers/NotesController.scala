package io.scalac.controllers

import java.util.UUID

import io.scalac.common.entities.{GenericResponse, PaginatedResponse, Pagination}
import io.scalac.common.logger.Logging
import io.scalac.common.play._
import io.scalac.common.services.{InvalidResource, MissingResource, ServiceProfiler}
import io.scalac.domain.services.NotesService
import io.scalac.domain.services.transport.{IncomingNote, UpdateNote}
import monix.execution.Scheduler
import play.api.mvc._

class NotesController (
  notesService: NotesService,
  scheduler: Scheduler)
  (implicit profiler: ServiceProfiler, controllerComponents: ControllerComponents)
  extends AbstractController(controllerComponents) with Logging with ControllerHelper {

  import Serializers._
  import io.scalac.common.play.serializers.Serializers._

  private implicit val schedulerImpl = scheduler

  def all(limit: Int, offset: Int) = noEntity { implicit request => implicit ctx => implicit corr =>
    val pagination = Pagination(limit = limit, offset = offset)
    notesService.list(pagination).runAsync.map {
      _.fold(
        otherErrorsHandler,
        notes => {
          logger.info(s"${request.path} - successful response")
          val response = PaginatedResponse(
            pagination.increase, notes
          )
          Ok(response.asJson)
        }
      )
    }
  }

  def find(noteId: UUID) = noEntity { implicit request => implicit ctx => implicit corr =>
    notesService.find(noteId).runAsync.map {
      _.fold(
        otherErrorsHandler,
        noteOpt => {
          logger.info(s"${request.path} - successful response")
          noteOpt match {
            case Some(note) => Ok(note.asJson)
            case None => NotFound(GenericResponse(s"Note [$noteId] does not exist").asJson)
          }
        }
      )
    }
  }

  def update(noteId: UUID) = withParsedEntity[IncomingNote] { noteToUpdate => implicit request => implicit ctx => implicit corr =>
    notesService.update(UpdateNote(noteId, noteToUpdate)).runAsync.map {
      _.fold (
        handler {
          case MissingResource(msg) =>
            logger.info(s"${request.path} - $msg")
            NotFound(GenericResponse(msg).asJson)
          case InvalidResource(errors) =>
            logger.info(s"${request.path} - Cannot update note with invalid request")
            BadRequest(GenericResponse(s"Invalid body: ${errors.mkString("\n")}").asJson)
        }.orElse(otherErrorsHandler),
        outgoingNote => {
          logger.info(s"${request.path} - successful response")
          Ok(outgoingNote.asJson)
        }
      )
    }
  }

  def create() = withParsedEntity[IncomingNote] { newNote => implicit request => implicit ctx => implicit corr =>
    notesService.create(newNote).runAsync.map {
      _.fold(
        otherErrorsHandler,
        newUUID => {
          logger.info(s"${request.path} - successful response")
          Created(GenericResponse(newUUID.toString).asJson)
        }
      )
    }
  }

}
