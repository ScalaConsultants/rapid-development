package io.scalac.controllers

import java.util.UUID

import scala.concurrent.Future

import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import monix.execution.Scheduler
import play.api.mvc.{Action, Controller}

import io.scalac.common.auth
import io.scalac.common.core.Correlation
import io.scalac.common.logger.Logging
import io.scalac.common.play.{GenericResponse, PaginatedResponse, Pagination}
import io.scalac.common.services.{EmptyResponse, ServiceProfiler}
import io.scalac.services.{IncomingNote, NotesService, UpdateNote}

@Singleton
class NotesController @Inject()(
  notesService: NotesService,
  @Named("DefaultScheduler") implicit val scheduler: Scheduler,
  implicit val profiler: ServiceProfiler
) extends Controller with Logging {

  import Serializers._
  import io.scalac.common.play.serializers.Serializers._

  def all(limit: Int, offset: Int) = Action.async { request =>
    implicit val emptyContext = auth.EmptyContext()
    implicit val cid = Correlation.getCorrelation(request.headers.toSimpleMap)
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

  def find(noteId: UUID) = Action.async { request =>
    implicit val emptyContext = auth.EmptyContext()
    implicit val cid = Correlation.getCorrelation(request.headers.toSimpleMap)
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
    implicit val cid = Correlation.getCorrelation(request.headers.toSimpleMap)
    logger.info(s"${request.path}")
    request.body.validate[IncomingNote].fold(
      invalid => Future.successful(BadRequest(GenericResponse(s"Invalid body: ${invalid.mkString(" ")}").asJson)),
      noteToUpdate => {
        notesService.update(UpdateNote(noteId, noteToUpdate)).runAsync.map {
          _.fold(
            {
              case EmptyResponse(msg) =>
                logger.info(msg)
                NotFound(GenericResponse(msg).asJson)
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

  def create() = Action.async(parse.json) { request =>
    implicit val emptyContext = auth.EmptyContext()
    implicit val cid = Correlation.getCorrelation(request.headers.toSimpleMap)
    logger.info(s"${request.path}")
    request.body.validate[IncomingNote].fold(
      invalid => Future.successful(BadRequest(GenericResponse(s"Invalid body: ${invalid.mkString(" ")}").asJson)),
      newNote => {
        notesService.create(newNote).runAsync.map {
          _.fold(
            serviceError => {
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
}
