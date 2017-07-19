package io.scalac.controllers.auth

import com.mohiva.play.silhouette.api.Silhouette
import monix.execution.Scheduler
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import io.scalac.common.auth.BearerTokenEnv
import io.scalac.common.entities.GenericResponse
import io.scalac.common.logger.Logging
import io.scalac.common.play._
import io.scalac.common.play.serializers.Serializers._
import io.scalac.common.services._
import io.scalac.services.auth.{SignInRequest, SigningService, SingUpRequest}

class SigningController (
  silhouette: Silhouette[BearerTokenEnv],
  signUpService: SigningService,
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
        signUpService.signUp(SingUpRequest(incomingSignUp, request)).runAsync.map(
          _.fold(
            handler[AuthError] {
              case UserUnauthorized =>
                logger.error(s"${request.path} - user [${incomingSignUp.email}] failed to register")
                Unauthorized(GenericResponse("Failed to register").asJson)
              case UserAlreadyExists =>
                logger.info(s"${request.path} - user [${incomingSignUp.email}] already exists")
                Ok(GenericResponse("User already registered").asJson)
            }.orElse(otherErrorsHandler),
            generatedToken => {
              logger.info("Successfully generated token for after signing up")
              Ok(Json.obj("token" -> generatedToken.id))
            }
          )
        )

      }
    )
  }

  def signIn = silhouette.UnsecuredAction.async(parse.json) { implicit request: Request[JsValue] =>
    implicit val emptyContext = EmptyContext()
    implicit val c = request.attrs(RequestAttributes.Correlation)

    logger.info(s"${request.path}")

    request.body.validate[IncomingSignIn].fold(
      invalid => badRequestFuture(s"Invalid body: ${invalid.mkString(" ")}"),
      data => {
        signUpService.signIn(SignInRequest(data, request)).runAsync.map(
          _.fold(
            handler[AuthError] {
              case UserUnauthorized =>
                logger.info(s"${request.path} - user [${data.email}] unauthorized")
                Unauthorized
              case UserNotFound =>
                logger.info(s"${request.path} - user [${data.email}] not found")
                Unauthorized
            }.orElse(otherErrorsHandler),
            generatedToken => {
              logger.info("Successfully generated token after signing in")
              Ok(Json.obj("token" -> generatedToken.id))
            }
          )
        )

      }
    )
  }

}
