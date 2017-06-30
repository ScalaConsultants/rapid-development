package io.scalac.controllers

import java.util.UUID

import io.scalac.common.auth
import io.scalac.common.logger.Logging
import io.scalac.common.play.{GenericResponse, PaginatedResponse, Pagination, RequestAttributes}
import io.scalac.common.services.{InvalidResource, MissingResource, ServiceProfiler}
import io.scalac.services.{IncomingNote, NotesService, UpdateNote}
import monix.execution.Scheduler
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.Future

class NotesController (
  notesService: NotesService,
  scheduler: Scheduler)
  (implicit profiler: ServiceProfiler, controllerComponents: ControllerComponents)
  extends AbstractController(controllerComponents) with Logging {

  import Serializers._
  import io.scalac.common.play.serializers.Serializers._

  private implicit val schedulerImpl = scheduler

  def all(limit: Int, offset: Int) = Action.async(parse.default) { request =>
    implicit val emptyContext = auth.EmptyContext()
    implicit val c = request.attrs(RequestAttributes.Correlation)
    logger.info(s"${request.path}")
    val pagination = Pagination(limit = limit, offset = offset)
    notesService.list(pagination).runAsync.map {
      _.fold(
        serviceError => {
          val msg = s"Failed due to: $serviceError"
          logger.error(s"${request.path} - $msg")
          InternalServerError(GenericResponse(msg).asJson)
        },
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

  def find(noteId: UUID) = Action.async(parse.default) { request =>
    implicit val emptyContext = auth.EmptyContext()
    implicit val c = request.attrs(RequestAttributes.Correlation)
    logger.info(s"${request.path}")
    notesService.find(noteId).runAsync.map {
      _.fold(
        serviceError => {
          val msg = s"Failed due to: $serviceError"
          logger.error(s"${request.path} - $msg")
          InternalServerError(GenericResponse(msg).asJson)
        },
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

  def update(noteId: UUID) = Action.async(parse.json) { request =>
    implicit val emptyContext = auth.EmptyContext()
    implicit val c = request.attrs(RequestAttributes.Correlation)
    logger.info(s"${request.path}")
    request.body.validate[IncomingNote].fold(
      invalid => badRequestFuture(s"Invalid body: ${invalid.mkString("\n")}"),
      noteToUpdate => {
        notesService.update(UpdateNote(noteId, noteToUpdate)).runAsync.map {
          _.fold(
            {
              case MissingResource(msg) =>
                logger.info(msg)
                NotFound(GenericResponse(msg).asJson)
              case InvalidResource(errors) =>
                logger.info("Cannot update note with invalid request")
                BadRequest(GenericResponse(s"Invalid body: ${errors.mkString("\n")}").asJson)
              case serviceError =>
                val msg = s"Failed due to: $serviceError"
                logger.error(s"${request.path} - $msg")
                InternalServerError(GenericResponse(msg).asJson)
            },
            newUUID => {
              logger.info(s"${request.path} - successful response")
              Created(GenericResponse(newUUID.toString).asJson)
            }
          )
        }
      }
    )
  }

  def create() = Action.async(parse.anyContent) { request =>
    implicit val emptyContext = auth.EmptyContext()
    implicit val c = request.attrs(RequestAttributes.Correlation)
    logger.info(s"${request.path}")
    request.body.asJson.map(_.validate[IncomingNote].fold(
      invalid => badRequestFuture(s"Invalid body: ${invalid.mkString(" ")}"),
      newNote => {
        notesService.create(newNote).runAsync.map {
          _.fold(
            {
              case serviceError =>
                val msg = s"Failed due to: $serviceError"
                logger.error(s"${request.path} - $msg")
                InternalServerError(GenericResponse(msg).asJson)
            },
            newUUID => {
              logger.info(s"${request.path} - successful response")
              Created(GenericResponse(newUUID.toString).asJson)
            }
          )
        }
      }
    )).getOrElse(badRequestFuture("Body is a not a correct JSON"))
  }

  private def badRequestFuture(msg: String) =
    Future.successful(BadRequest(GenericResponse(msg).asJson))
}
