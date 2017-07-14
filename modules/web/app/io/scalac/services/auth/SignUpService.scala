package io.scalac.services.auth

import java.util.UUID

import cats.syntax.either._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import monix.cats.monixToCatsMonad
import monix.eval.Task

import io.scalac.common.auth.AuthUser
import io.scalac.common.logger.Logging
import io.scalac.common.services._
import io.scalac.common.syntax._
import io.scalac.controllers.auth.{AuthToken, IncomingSignUp}
import io.scalac.domain.entities.User

trait SignUpService {

  def signUp: Service[IncomingSignUp, AuthToken, ServiceError]
}

class DefaultSignUpService(
  authUserService: AuthUserService,
  authInfoRepository: AuthInfoRepository, //grrr
  passwordHasherRegistry: PasswordHasherRegistry,
  authTokenService: AuthTokenService,
  clock: Clock
)(implicit val profiler: ServiceProfiler) extends SignUpService with Logging {

  override def signUp: Service[IncomingSignUp, AuthToken, ServiceError] =
    Service("io.scalac.services.auth.DefaultSignUpService.signUp") { req => implicit ctx =>

      def addAuthInfo(loginInfo: LoginInfo, authInfo: PasswordInfo): Task[Either[ServiceError, PasswordInfo]] = {
        Task.deferFutureAction(implicit scheduler => authInfoRepository.add(loginInfo, authInfo))
          .map(_.asRight[ServiceError])
          .onErrorHandle { ex =>
            ServiceFailed(ex.getMessage).asLeft[PasswordInfo]
          }
      }

      //TODO validate IncomingSignUp data
      val loginInfo = LoginInfo(CredentialsProvider.ID, req.email)
      Task.deferFutureAction(implicit scheduler => authUserService.retrieve(loginInfo)).flatMap {
        case Some(_) =>
          Task.now(UserExists.asLeft[AuthToken])

        case None =>
          val authInfo = passwordHasherRegistry.current.hash(req.password)
          val now = clock.now
          val authUser = AuthUser(
            loginInfo = loginInfo,
            user = User (
              id = Some(UUID.randomUUID()),
              firstName = req.firstName,
              lastName = req.lastName,
              email = req.email,
              avatarURL = None,
              createdAt = now,
              updatedAt = now,
              version = Some(1)
              )
            )

          (for {
            userId    <- authUserService.save(authUser).eitherT
            _         <- addAuthInfo(loginInfo, authInfo).eitherT
            authToken <- authTokenService.create(userId).eitherT
          } yield AuthToken(authToken)).value
      }
    }
}
