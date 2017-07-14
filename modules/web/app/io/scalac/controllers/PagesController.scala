package io.scalac.controllers

import controllers.AssetsFinder

import io.scalac.common.entities.{GenericResponse, PaginatedResponse, Pagination}
import io.scalac.common.logger.Logging
import io.scalac.common.play.RequestAttributes
import io.scalac.common.services._
import io.scalac.domain.services.NotesService
import monix.execution.Scheduler
import play.api.mvc.{AbstractController, ControllerComponents}

class PagesController (
  notesService: NotesService,
  assetsFinder: AssetsFinder,
  scheduler: Scheduler
)(implicit profiler: ServiceProfiler, controllerComponents: ControllerComponents)
  extends AbstractController(controllerComponents) with Logging {

  private implicit val schedulerImpl = scheduler

  def index() = Action.async(parse.default) { request =>
    implicit val emptyContext = EmptyContext()
    implicit val c = request.attrs(RequestAttributes.Correlation)
    logger.info(s"${request.path}")
    val pagination = Pagination(limit = 5, offset = 0)
    notesService.list(pagination).runAsync.map {
      _.fold(
        serviceError => {
          val msg = s"Failed due to: $serviceError"
          logger.error(s"${request.path} - $msg")
          InternalServerError(views.html.serverError(GenericResponse(msg))(assetsFinder))
        },
        notes => {
          logger.info(s"${request.path} - successful response")
          val response = PaginatedResponse(
            pagination.increase, notes
          )
          Ok(views.html.index(response)(assetsFinder))
        }
      )
    }
  }
}
