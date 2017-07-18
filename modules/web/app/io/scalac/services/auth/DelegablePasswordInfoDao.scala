package io.scalac.services.auth

import java.util.UUID

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.exceptions.AuthenticatorRetrievalException
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import io.scalac.common.core.AuthenticationProviderId
import io.scalac.common.db.DBExecutor
import io.scalac.common.services.AppClock
import io.scalac.domain.entities._

class DelegablePasswordInfoDao(
  authProviderRepo: AuthenticationProvidersSlickPostgresRepository,
  passwordRepo: PasswordInformationSlickPostgresRepository,
  appClock: AppClock,
  dbExecutor: DBExecutor
) extends DelegableAuthInfoDAO[PasswordInfo] with CommonMappers {

  import dbExecutor._
  import dbExecutor.dbConfig.profile.api._
  implicit val ec = dbExecutor.scheduler

  //TODO unclear to me if I should here also store/remove LoginInfo itself, but rather not

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    val q = for {
      (_, passInfo) <- authProviderRepo.tableQuery join passwordRepo.tableQuery on (_.id === _.authProviderId)
    } yield passInfo

    evalFuture {
      Compiled(q).result.headOption.map(_.map(p => PasswordInfo(p.hasher, p.password, p.salt)))
    }
  }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    for {
      authProvider <- getAuthProvider(loginInfo)
      p            <- storePasswordInformation(authProvider.id.get, authInfo)
    } yield PasswordInfo(p.hasher, p.password, p.salt)
  }

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    for {
      authProvider     <- getAuthProvider(loginInfo)
      existingPassInfo <- getPasswordInformation(authProvider.id.get)
      updated          <- updatePasswordInformation(existingPassInfo, authInfo)
    } yield PasswordInfo(updated.hasher, updated.password, updated.salt)
  }

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    for {
      authProvider  <- getAuthProvider(loginInfo)
      maybePassInfo <- findPasswordInformation(authProvider.id.get)
      entity        <- maybePassInfo.fold(storePasswordInformation(authProvider.id.get, authInfo)) {
        existing => updatePasswordInformation(existing, authInfo)
      }
    } yield PasswordInfo(entity.hasher, entity.password, entity.salt)
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    for {
      authProvider     <- getAuthProvider(loginInfo)
      existingPassInfo <- getPasswordInformation(authProvider.id.get)
      _                <- evalFuture(passwordRepo.delete(existingPassInfo))
    } yield ()
  }

  private def getAuthProvider(loginInfo: LoginInfo): Future[AuthenticationProvider] = {
    evalFuture {
      authProviderRepo.findByProviderIdAndKey(loginInfo.providerID,loginInfo.providerKey).result.headOption
    }.map(_.getOrElse(throw new AuthenticatorRetrievalException("No authentication provider found")))
  }

  private def storePasswordInformation(authenticationProviderId: AuthenticationProviderId,
    authInfo: PasswordInfo): Future[PasswordInformation] = {
    evalFuture {
      passwordRepo.save(PasswordInformation(
        id = Some(UUID.randomUUID()),
        authenticationProviderId = authenticationProviderId,
        hasher = authInfo.hasher,
        password = authInfo.password,
        salt = authInfo.salt,
        createdAt = appClock.now
      ))
    }
  }

  private def updatePasswordInformation(existing: PasswordInformation,
    authInfo: PasswordInfo): Future[PasswordInformation] = {
    evalFuture {
      passwordRepo.update(
        existing.copy(
          hasher = authInfo.hasher,
          password = authInfo.password,
          salt = authInfo.salt
        )
      )
    }
  }

  private def findPasswordInformation(authProviderId: AuthenticationProviderId): Future[Option[PasswordInformation]] = {
    evalFuture {
      passwordRepo.findByAuthProviderId(authProviderId).result.headOption
    }
  }

  private def getPasswordInformation(authProviderId: AuthenticationProviderId): Future[PasswordInformation] = {
    findPasswordInformation(authProviderId).map(_.getOrElse(throw new AuthenticatorRetrievalException("No authentication provider found")))
  }
}
