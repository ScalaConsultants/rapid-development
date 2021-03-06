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
import io.scalac.services.auth.{AuthorizationService, SignInRequest, SingUpRequest}

class AuthorizationController (
  silhouette: Silhouette[BearerTokenEnv],
  authorizationService: AuthorizationService,
  scheduler: Scheduler
)(implicit profiler: ServiceProfiler, controllerComponents: ControllerComponents)
  extends AbstractController(controllerComponents) with Logging with ControllerHelper {

  implicit val ex = scheduler

  //TODO provide support with withParsedEntity
  def signUp = silhouette.UnsecuredAction.async(parse.json) { implicit request: Request[JsValue] =>
    implicit val ctx = getServiceContext
    implicit val c = ctx.correlation

    logger.info(s"${request.path}")

    request.body.validate[IncomingSignUp].fold(
      invalid => badRequestFuture(s"Invalid body: ${invalid.mkString(" ")}"),
      incomingSignUp => {
        authorizationService.signUp(SingUpRequest(incomingSignUp, request)).runAsync.map(
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
    implicit val ctx = getServiceContext
    implicit val c = ctx.correlation

    logger.info(s"${request.path}")

    request.body.validate[IncomingSignIn].fold(
      invalid => badRequestFuture(s"Invalid body: ${invalid.mkString(" ")}"),
      data => {
        authorizationService.signIn(SignInRequest(data, request)).runAsync.map(
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

  def signOut = silhouette.SecuredAction.async { implicit request =>
//    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    implicit val ctx = getServiceContext
    implicit val c = ctx.correlation

    logger.info(s"${request.path}")
    silhouette.env.authenticatorService.discard(request.authenticator, Ok).map { result =>
      logger.info(s"Successfully logged out user")
      result
    }
  }

}
