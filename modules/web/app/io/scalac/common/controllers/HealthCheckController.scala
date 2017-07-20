package io.scalac.common.controllers

import monix.execution.Scheduler
import play.api.libs.json._
import play.api.mvc._

import io.scalac.common.logger.Logging
import io.scalac.common.play.ControllerHelper
import io.scalac.common.services.{ExternalHealthCheckResponse, HealthCheckRequest, HealthCheckResponse, _}

class HealthCheckController (
  healthCheckServices: HealthCheckServices,
  scheduler: Scheduler)(
  implicit profiler: ServiceProfiler,
  controllerComponents: ControllerComponents
) extends AbstractController(controllerComponents)
  with ControllerHelper
  with Logging {

  implicit val externalHealthCheckWrites = Json.writes[ExternalHealthCheckResponse]
  implicit val healthCheckWrites = Json.writes[HealthCheckResponse]
  implicit val ex: Scheduler = scheduler

  def healthCheck(diagnostics: Boolean): Action[AnyContent] = Action.async(parse.default) { implicit request =>
    implicit val ctx = getServiceContext
    implicit val c = ctx.correlation
    logger.info(s"${request.path} - getting app status")
    val future = healthCheckServices.healthCheck(HealthCheckRequest(diagnostics)).runAsync
    future.map { either_ =>
      either_.fold(
        serviceError => Ok(s"Failed due to: $serviceError"),
        response => Ok(Json.prettyPrint(Json.toJson(response)))
      )
    }
  }
}
