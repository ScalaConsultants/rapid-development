package io.scalac.common.play

import scala.concurrent.Future
import scala.language.implicitConversions

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import play.api.libs.json.Reads
import play.api.mvc._

import io.scalac.common.auth.AuthUser
import io.scalac.common.core.Correlation
import io.scalac.common.entities.GenericResponse
import io.scalac.common.logger.Logging
import io.scalac.common.services._

trait ControllerHelper { self: Logging with AbstractController =>
  import io.scalac.common.play.serializers.Serializers._

  def badRequestFuture(msg: String) =
    Future.successful(BadRequest(GenericResponse(msg).asJson))

  def noEntity(fun: Request[AnyContent] => ServiceContext => Correlation => Future[Result]): Action[AnyContent] =
    Action.async(parse.anyContent) { request =>
      implicit val ctx = getServiceContext(request)
      implicit val c = ctx.correlation

      logger.info(s"${request.path}")

      fun(request)(ctx)(c)
    }

  def withParsedEntity[A : Reads](fun: A => Request[AnyContent] => ServiceContext => Correlation => Future[Result]): Action[AnyContent] =
    Action.async(parse.anyContent) { request =>
      implicit val ctx = getServiceContext(request)
      implicit val c = ctx.correlation

      logger.info(s"${request.path}")

      request.body.asJson.map(_.validate[A].fold(
        invalid => badRequestFuture(s"Invalid body: ${invalid.mkString(" ")}"),
        parsedEntity => {
          fun(parsedEntity)(request)(ctx)(c)
        }
      )).getOrElse(badRequestFuture("Body is a not a correct JSON"))
    }

  def handler[L](pf: PartialFunction[L, Result]): PartialFunction[L, Result] = pf

  def otherErrorsHandler[L, T](implicit request: Request[T], corr: Correlation): PartialFunction[L, Result] = {
    case serviceError =>
      val msg = s"Failed due to: $serviceError"
      logger.error(s"${request.path} - $msg")
      InternalServerError(GenericResponse(msg).asJson)
  }

  def getServiceContext[C](implicit request: Request[C]) = request match {
    case SecuredRequest(identity, _, _) =>
      identity match {
        case AuthUser(_, user) =>
          val correlation = Correlation.getCorrelation(request.headers.toSimpleMap)
            .copy(userId = Some(user.id.get))
          ServiceContext(correlation, Map.empty, Some(user), None)

        case _ =>
          val correlation = Correlation.getCorrelation(request.headers.toSimpleMap)
          EmptyContext(correlation)
      }

    case _ =>
      val correlation = Correlation.getCorrelation(request.headers.toSimpleMap)
      EmptyContext(correlation)
  }

  implicit def fromContextToCorrelation(ctx: ServiceContext): Correlation = ctx.correlation
}
