package io.scalac.common.play

import scala.concurrent._

import _root_.controllers.AssetsFinder
import org.slf4j.{LoggerFactory, MarkerFactory}
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router
import play.core.SourceMapper

import io.scalac.common.entities.GenericResponse

class RootHttpErrorHandler(
  env: Environment,
  config: Configuration,
  sourceMapper: Option[SourceMapper],
  router: Option[Router],
  assetsFinder: AssetsFinder
)(implicit executionContext: ExecutionContext)
  extends DefaultHttpErrorHandler(env, config, sourceMapper, router)
    with InstrumentedErrorHandler {

  private val logger = LoggerFactory.getLogger(getClass)

  override implicit val ec: ExecutionContext = executionContext

  override protected def onDevServerError(request: RequestHeader, e: UsefulException): Future[Result] = {
    Future.successful(InternalServerError(Json.obj(
      "error" -> Json.obj("message" -> e.getMessage, "errorId" -> e.id))))
  }

  override def onProdServerError(request: RequestHeader, e: UsefulException): Future[Result] =
    Future.successful(InternalServerError(views.html.serverError(GenericResponse("Please check logs"))(assetsFinder)))

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    Future.successful(new Status(statusCode))

  override def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    Future.successful(env.mode match {
      case Mode.Prod =>
        NotFound(
          Json.obj("error" -> Json.obj(
            "title" -> s"${request.uri} with ${request.method} does not exist",
            "message" -> message)
          )
        )

      case _ =>
        NotFound(views.html.defaultpages.devNotFound(request.method, request.uri, Option(router.get)))
    })
  }

  /** Logging every 5XX error */
  override def logServerError(request: RequestHeader, usefulException: UsefulException): Unit = {
    val (method, uri) = (request.method, request.uri)
    val internalErrorMarker = MarkerFactory.getMarker("INTERNAL")
    val msg = s"${usefulException.id} - Internal server error, for ($method) [$uri]"
    logger.error(internalErrorMarker, msg, usefulException)
  }

  override def onBadRequest(request: RequestHeader, message: String): Future[Result] =
    Future.successful(BadRequest(Json.obj("error" -> Json.obj("message" -> ("Bad Request:" + message)))))
}
