package io.scalac.common.play

import scala.concurrent._
import scala.util.control.NonFatal

import _root_.controllers.AssetsFinder
import com.mohiva.play.silhouette.api.exceptions.{NotAuthenticatedException, NotAuthorizedException, SilhouetteException}
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
    logger.error(s"Unhandled DEV server error", e)
    Future.successful(InternalServerError(Json.obj(
      "error" -> Json.obj("message" -> e.getMessage, "errorId" -> e.id))))
  }

  override def onProdServerError(request: RequestHeader, e: UsefulException): Future[Result] = {
    handleSilhouetteExceptions.applyOrElse(e.cause, (t: Throwable) => t match {
      case NonFatal(_) =>
        Future.successful(InternalServerError(
          views.html.serverError(GenericResponse("Please check logs"))(assetsFinder)))
    })
  }

  private val handleSilhouetteExceptions: PartialFunction[Throwable, Future[Result]] = {
//    https://www.silhouette.rocks/v5.0/docs/error-handling
    case e: NotAuthorizedException =>
      logger.error("Received unhandled NotAuthorizedException", e)
      Future.successful(Forbidden)
    case e: NotAuthenticatedException =>
      logger.error("Received unhandled NotAuthenticatedException", e)
      Future.successful(Unauthorized)
    case e: SilhouetteException =>
      logger.error("Received unhandled SilhouetteException", e)
      Future.successful(Unauthorized)
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    logger.error(s"Received client error with status code [$statusCode] and message [$message]")
    Future.successful(new Status(statusCode))
  }

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
