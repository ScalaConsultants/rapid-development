package io.scalac.controllers

import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import monix.execution.Scheduler
import play.api.mvc.InjectedController

import io.scalac.common.auth
import io.scalac.common.logger.Logging
import io.scalac.common.play.{GenericResponse, PaginatedResponse, Pagination, RequestAttributes}
import io.scalac.common.services.ServiceProfiler
import io.scalac.services.NotesService

@Singleton
class PagesController @Inject()(
  notesService: NotesService,
  @Named("DefaultScheduler") implicit val scheduler: Scheduler,
  implicit val profiler: ServiceProfiler
) extends InjectedController with Logging {

  def index() = Action.async(parse.default) { request =>
    implicit val emptyContext = auth.EmptyContext()
    implicit val c = request.attrs(RequestAttributes.Correlation)
    logger.info(s"${request.path}")
    val pagination = Pagination(limit = 5, offset = 0)
    notesService.list(pagination).runAsync.map {
      _.fold(
        serviceError => {
          val msg = s"Failed due to: $serviceError"
          logger.error(s"${request.path} - $msg")
          InternalServerError(views.html.serverError(GenericResponse(msg)))
        },
        notes => {
          logger.info(s"${request.path} - successful response")
          val response = PaginatedResponse(
            pagination.increase, notes
          )
          Ok(views.html.index(response))
        }
      )
    }
  }
}
