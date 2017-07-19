package io.scalac.common.play

import io.scalac.common.core.Correlation
import io.scalac.common.entities.GenericResponse
import io.scalac.common.logger.Logging
import io.scalac.common.services._
import play.api.libs.json.Reads
import play.api.mvc._

import scala.concurrent.Future

trait ControllerHelper { self: Logging with AbstractController =>
  import io.scalac.common.play.serializers.Serializers._

  def badRequestFuture(msg: String) =
    Future.successful(BadRequest(GenericResponse(msg).asJson))

  def noEntity(fun: Request[AnyContent] => ServiceContext => Correlation => Future[Result]): Action[AnyContent] = Action.async(parse.anyContent) { request =>
    implicit val emptyContext = EmptyContext()
    implicit val c = request.attrs(RequestAttributes.Correlation)

    logger.info(s"${request.path}")

    fun(request)(emptyContext)(c)
  }

  def withParsedEntity[A : Reads](fun: A => Request[AnyContent] => ServiceContext => Correlation => Future[Result]): Action[AnyContent] = Action.async(parse.anyContent) { request =>
    implicit val emptyContext = EmptyContext()
    implicit val c = request.attrs(RequestAttributes.Correlation)

    logger.info(s"${request.path}")

    request.body.asJson.map(_.validate[A].fold(
      invalid => badRequestFuture(s"Invalid body: ${invalid.mkString(" ")}"),
      parsedEntity => {
        fun(parsedEntity)(request)(emptyContext)(c)
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
}
