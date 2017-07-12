package io.scalac.controllers

import java.util.UUID

import io.scalac.common.entities.{GenericResponse, PaginatedResponse, Pagination}
import io.scalac.common.logger.Logging
import io.scalac.common.play._
import io.scalac.common.services.{InvalidResource, MissingResource, ServiceProfiler}
import io.scalac.domain.entities.{CommissionType, PaymentType}
import io.scalac.domain.services.{Criteria, MerchantsService}
import io.scalac.domain.services.transport.{IncomingNote, UpdateNote}
import monix.execution.Scheduler
import org.joda.time.DateTime
import play.api.mvc._

import scala.util.Try

class MerchantsController(
                           merchantsService: MerchantsService,
                           scheduler: Scheduler)
                         (implicit profiler: ServiceProfiler, controllerComponents: ControllerComponents)
  extends AbstractController(controllerComponents) with Logging with ControllerHelper {

  import Serializers._
  import io.scalac.common.play.serializers.Serializers._

  private implicit val schedulerImpl = scheduler

  def all(limit: Int, offset: Int) = noEntity { implicit request => implicit ctx => implicit corr =>
    val pagination = Pagination(limit, offset)
    val criteria = {
      val paymentType     = request.getQueryString("paymentType").flatMap { str =>
        Try(PaymentType.withName(str)).toOption
      }

      val commissionType  = request.getQueryString("commissionType").flatMap { str =>
        Try(CommissionType.withName(str)).toOption
      }

      val lastInvoiceDate = request.getQueryString("lastInvoiceDate").flatMap(str => parseDateTime(str).toOption)
      Criteria(paymentType, commissionType, lastInvoiceDate)
    }

    merchantsService.findByCriteria(criteria, pagination).runAsync.map {
      _.fold(
        otherErrorsHandler,
        merchants => {
          logger.info(s"${request.path} - successful response")
          Ok(merchants.asJson)
        }
      )
    }
  }

//  def find(noteId: UUID) = noEntity { implicit request => implicit ctx => implicit corr =>
//    notesService.find(noteId).runAsync.map {
//      _.fold(
//        otherErrorsHandler,
//        noteOpt => {
//          logger.info(s"${request.path} - successful response")
//          noteOpt match {
//            case Some(note) => Ok(note.asJson)
//            case None => NotFound(GenericResponse(s"Note [$noteId] does not exist").asJson)
//          }
//        }
//      )
//    }
//  }
//
//  def update(noteId: UUID) = withParsedEntity[IncomingNote] { noteToUpdate => implicit request => implicit ctx => implicit corr =>
//    notesService.update(UpdateNote(noteId, noteToUpdate)).runAsync.map {
//      _.fold (
//        handler {
//          case MissingResource(msg) =>
//            logger.info(msg)
//            NotFound(GenericResponse(msg).asJson)
//          case InvalidResource(errors) =>
//            logger.info("Cannot update note with invalid request")
//            BadRequest(GenericResponse(s"Invalid body: ${errors.mkString("\n")}").asJson)
//        }.orElse(otherErrorsHandler),
//        outgoingNote => {
//          logger.info(s"${request.path} - successful response")
//          Ok(outgoingNote.asJson)
//        }
//      )
//    }
//  }
//
//  def create() = withParsedEntity[IncomingNote] { newNote => implicit request => implicit ctx => implicit corr =>
//    notesService.create(newNote).runAsync.map {
//      _.fold(
//        otherErrorsHandler,
//        newUUID => {
//          logger.info(s"${request.path} - successful response")
//          Created(GenericResponse(newUUID.toString).asJson)
//        }
//      )
//    }
//  }

}
