package io.scalac.controllers

import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import monix.execution.Scheduler
import play.api.mvc.{Action, Controller}

import io.scalac.common.auth
import io.scalac.common.core.Correlation
import io.scalac.common.logger.Logging
import io.scalac.common.play.{GenericError, PaginatedResponse, Pagination}
import io.scalac.common.services.ServiceProfiler
import io.scalac.services.NotesService

@Singleton
class NotesController @Inject() (
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
    notesService.list(pagination).runAsync.map { _.fold(
      serviceError => {
        val msg = s"Failed due to: $serviceError"
        logger.error(s"${request.path} - msg")
        InternalServerError(GenericError(msg).asJson)
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
}
