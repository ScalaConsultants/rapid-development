package io.scalac.bootstrap

import scala.language.implicitConversions

import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, SilhouetteProvider}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.{CacheAuthenticatorRepository, DelegableAuthInfoRepository}
import play.api.cache.ehcache.EhCacheComponents
import play.api.mvc.{BodyParsers, PlayBodyParsers}
import play.api.{BuiltInComponents, Configuration, mvc}
import pureconfig.ConfigConvert.{catchReadError, viaNonEmptyString}
import pureconfig.{ConfigConvert, loadConfigOrThrow}

import io.scalac.common.auth.{BearerTokenEnv, CustomSecuredErrorHandler, CustomUnsecuredErrorHandler}
import io.scalac.domain.dao.UsersDao
import io.scalac.services.auth.{AuthUserService, DefaultAuthUsersService}

trait SilhouetteComponents
  extends EhCacheComponents
    with SecuredActionComponents
    with UnsecuredActionComponents
    with UserAwareActionComponents
    with SecuredErrorHandlerComponents
    with UnsecuredErrorHandlerComponents {

  val playBodyParsers: PlayBodyParsers
  val usersDao: UsersDao

  override lazy val securedErrorHandler: SecuredErrorHandler = new CustomSecuredErrorHandler()
  override lazy val unsecuredErrorHandler: UnsecuredErrorHandler = new CustomUnsecuredErrorHandler()

  val silhouetteDefaultBodyParser = new mvc.BodyParsers.Default(playBodyParsers)

  override def securedBodyParser: BodyParsers.Default = silhouetteDefaultBodyParser
  override def unsecuredBodyParser: BodyParsers.Default = silhouetteDefaultBodyParser
  override def userAwareBodyParser: BodyParsers.Default = silhouetteDefaultBodyParser

  val cacheLayer = new PlayCacheLayer(defaultCacheApi)
  val idGenerator = new SecureRandomIDGenerator()
  val passwordHasher = new BCryptPasswordHasher
  val passwordHasherRegistry = PasswordHasherRegistry(passwordHasher)
  val fingerprintGenerator = new DefaultFingerprintGenerator(includeRemoteAddress = false)
  val silhouetteEventBus = new EventBus()
  val silhouetteClock = Clock()

  val usersService = new DefaultAuthUsersService(usersDao)

  val silhouette = {
    val authenticatorService = provideAuthenticatorService(idGenerator, cacheLayer, configuration, silhouetteClock)
    val env = provideEnvironment(usersService, authenticatorService, silhouetteEventBus)
    new SilhouetteProvider[BearerTokenEnv](env, securedAction, unsecuredAction, userAwareAction)
  }

  //TODO
//  val authInfoRepository = provideAuthInfoRepository()

  def provideEnvironment(
    userService: AuthUserService,
    authenticatorService: AuthenticatorService[BearerTokenAuthenticator],
    eventBus: EventBus): Environment[BearerTokenEnv] = {

    Environment[BearerTokenEnv](
      userService,
      authenticatorService,
      requestProvidersImpl = Seq(),
      eventBus
    )
  }

  def provideAuthenticatorService(
    idGenerator: IDGenerator,
    cacheLayer: CacheLayer,
    configuration: Configuration,
    clock: Clock): AuthenticatorService[BearerTokenAuthenticator] = {

    implicit val localDateConfigConvert: ConfigConvert[RequestPart.Value] =
      viaNonEmptyString[RequestPart.Value](catchReadError(RequestPart.withName), _.toString)

    val config = loadConfigOrThrow[BearerTokenAuthenticatorSettings](configuration.underlying.getConfig("silhouette.authenticator"))

    //TODO add our repo instead of cache...
    val authenticatorRepository = new CacheAuthenticatorRepository[BearerTokenAuthenticator](cacheLayer)

    new BearerTokenAuthenticatorService(config, authenticatorRepository, idGenerator, clock)
  }

  def provideAuthInfoRepository(
    passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
    oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info]): AuthInfoRepository = {

    new DelegableAuthInfoRepository(passwordInfoDAO, oauth2InfoDAO)
  }
}
