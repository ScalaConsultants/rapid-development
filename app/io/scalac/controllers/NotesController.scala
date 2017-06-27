package io.scalac.controllers

import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import monix.execution.Scheduler
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import io.scalac.common.auth
import io.scalac.common.core.Correlation
import io.scalac.common.logger.Logging
import io.scalac.common.play.GenericError
import io.scalac.common.services.ServiceProfiler
import io.scalac.services.NotesService

@Singleton
class NotesController @Inject() (
  notesService: NotesService,
  @Named("DefaultScheduler") implicit val scheduler: Scheduler,
  implicit val profiler: ServiceProfiler
) extends Controller with Logging {

  import io.scalac.common.play.serializers.Serializers._
  import Serializers._

  def all() = Action.async { request =>
    implicit val emptyContext = auth.EmptyContext()
    implicit val cid = Correlation.getCorrelation(request.headers.toSimpleMap)
    logger.info(s"${request.path} - getting app status")
    val future = notesService.findAll().runAsync
    future.map { either_ =>
      either_.fold(
        serviceError => InternalServerError(GenericError(s"Failed due to: $serviceError").asJson),
        response => Ok(Json.prettyPrint(Json.toJson(response)))
      )
    }
  }
}
