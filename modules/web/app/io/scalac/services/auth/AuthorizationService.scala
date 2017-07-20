package io.scalac.services.auth

import java.util.UUID

import scala.concurrent.Future

import cats.syntax.either._
import com.mohiva.play.silhouette.api.exceptions.{ProviderException, SilhouetteException}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import monix.cats.monixToCatsMonad
import monix.eval.Task

import io.scalac.bootstrap.config.SilhouetteConfig
import io.scalac.common.auth.{AuthUser, BearerTokenEnv}
import io.scalac.common.core.{AuthorizationToken, UserId}
import io.scalac.common.logger.Logging
import io.scalac.common.services._
import io.scalac.common.syntax._
import io.scalac.domain.entities.User

trait AuthorizationService {

  def signUp: Service[SingUpRequest, AuthorizationToken, AuthError]
  def signIn: Service[SignInRequest, AuthorizationToken, AuthError]
}

class DefaultAuthorizationService(
  authUserService: AuthUserService,
  authInfoRepository: AuthInfoRepository, //grrr
  passwordHasherRegistry: PasswordHasherRegistry,
  authTokenService: AuthTokenService,
  credentialsProvider: CredentialsProvider,
  silhouette: Silhouette[BearerTokenEnv],
  silhouetteConfig: SilhouetteConfig,
  clock: AppClock
)(implicit val profiler: ServiceProfiler) extends AuthorizationService with Logging {

  override def signUp: Service[SingUpRequest, AuthorizationToken, AuthError] =
    Service("io.scalac.services.auth.DefaultAuthorizationService.signUp") { req => implicit ctx =>

      implicit val rh = req.requestHeader
      implicit val c = ctx.correlation

      //TODO validate IncomingSignUp data
      val singUpData = req.incomingSignUp
      val loginInfo = LoginInfo(CredentialsProvider.ID, singUpData.email)
      Task.deferFutureAction(implicit scheduler => authUserService.retrieve(loginInfo)).flatMap {
        case Some(user) =>
          logger.info(s"Found already existing user during sign up, [userId=${user.user.id.get}]")
          Task.now(UserAlreadyExists.asLeft[AuthorizationToken])

        case None =>
          val authInfo = passwordHasherRegistry.current.hash(singUpData.password)
          val now = clock.now
          val authUser = AuthUser(
            loginInfo = loginInfo,
            user = User (
              id = Some(UserId(UUID.randomUUID())),
              firstName = singUpData.firstName,
              lastName = singUpData.lastName,
              email = singUpData.email,
              avatarURL = None,
              createdAt = now,
              updatedAt = now,
              version = Some(1)
              )
            )

          (for {
            userId        <- authUserService.store(authUser).eitherT
            _             <- authInfoRepository.add(loginInfo, authInfo).asAuthCall.eitherT
//            _             <- authTokenService.create(userId).eitherT //TODO not needed? https://github.com/mohiva/play-silhouette-angular-seed/blob/master/app/controllers/SignUpController.scala
            authenticator <- silhouette.env.authenticatorService.create(loginInfo).asAuthCall.eitherT
            token         <- silhouette.env.authenticatorService.init(authenticator).asAuthCall.eitherT
          } yield AuthorizationToken(token)).value

        //              silhouette.env.eventBus.publish(SignUpEvent(user, request))
        //              silhouette.env.eventBus.publish(LoginEvent(user, request))
      }.onErrorRecoverWith {
        case e: SilhouetteException =>
          logger.warn(s"Failed to authorize due to: [${e.getMessage}]", e)
          Task.now(AuthFailed("SilhouetteException occurred while signing up").asLeft)
      }
    }

  override def signIn: Service[SignInRequest, AuthorizationToken, AuthError] =
    Service("io.scalac.services.auth.DefaultAuthorizationService.signIn") { req => implicit ctx =>

      implicit val reqHeader = req.requestHeader
      implicit val c = ctx.correlation

      //TODO validate IncomingSignUp data

      val credentials = Credentials(req.incomingSignIn.email, req.incomingSignIn.password)
      Task.deferFutureAction { implicit scheduler =>
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          authUserService.retrieve(loginInfo).flatMap {
            //case Some(user) if !user.activated =>
            //  Future.successful(Ok(views.html.activateAccount(data.email)))
            case Some(_) =>
              silhouette.env.authenticatorService.create(loginInfo).map {
                case authenticator if req.incomingSignIn.rememberMe =>
                  authenticator.copy(
                    expirationDateTime = clock.now.plus(silhouetteConfig.authenticator.rememberMe.expiry.toMillis),
                    idleTimeout = Some(silhouetteConfig.authenticator.rememberMe.idleTimeout)
                  )
                case authenticator => authenticator
              }.flatMap { authenticator =>
                //silhouette.env.eventBus.publish(LoginEvent(user, request))
                silhouette.env.authenticatorService.init(authenticator).map(token => AuthorizationToken(token).asRight[AuthError])
              }

            case None => Future.successful(UserNotFound.asLeft)
          }
        }
      }.onErrorRecoverWith {
        case e: ProviderException =>
          logger.info(s"Invalid credentials: [${e.getMessage}]")
          Task.now(UserUnauthorized.asLeft)
        case e: SilhouetteException =>
          logger.warn(s"Failed to authorize due to: [${e.getMessage}]", e)
          Task.now(UserUnauthorized.asLeft)
      }
    }
}
