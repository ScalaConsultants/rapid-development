package io.scalac.controllers.auth

import com.mohiva.play.silhouette.api.{SignUpEvent, Silhouette}
import monix.execution.Scheduler
import play.api.libs.json.JsValue
import play.api.mvc._

import io.scalac.common.auth.BearerTokenEnv
import io.scalac.common.entities.GenericResponse
import io.scalac.common.logger.Logging
import io.scalac.common.play._
import io.scalac.common.services._
import io.scalac.services.auth.SignUpService
import io.scalac.common.play.serializers.Serializers._
import io.scalac.controllers.Serializers._

class SignUpController (
  silhouette: Silhouette[BearerTokenEnv],
  signUpService: SignUpService,
  scheduler: Scheduler
)(implicit profiler: ServiceProfiler, controllerComponents: ControllerComponents)
  extends AbstractController(controllerComponents) with Logging with ControllerHelper {

  implicit val ex = scheduler

  //TODO provide support with withParsedEntity
  def signUp = silhouette.UnsecuredAction.async(parse.json) { implicit request: Request[JsValue] =>
    implicit val emptyContext = EmptyContext()
    implicit val c = request.attrs(RequestAttributes.Correlation)

    logger.info(s"${request.path}")

    request.body.validate[IncomingSignUp].fold(
      invalid => badRequestFuture(s"Invalid body: ${invalid.mkString(" ")}"),
      incomingSignUp => {

        signUpService.signUp(incomingSignUp).runAsync.map(
          _.fold(
            handler {
              case UserExists =>
                logger.info(s"${request.path} - user [${incomingSignUp.email}] already exists")
                Ok(GenericResponse("AuthUser exists").asJson) //FIXME exposing data...
            }.orElse(otherErrorsHandler),
            generatedToken => {
//              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              logger.info("Successfully generated token for new user")
              Ok(generatedToken.asJson)
            }
          )
        )

      }
    )

  }

}
