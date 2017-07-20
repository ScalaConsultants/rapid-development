package io.scalac.bootstrap

import scala.language.implicitConversions

import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, SilhouetteProvider}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.repositories.{CacheAuthenticatorRepository, DelegableAuthInfoRepository}
import play.api.cache.ehcache.EhCacheComponents
import play.api.mvc.BodyParsers
import play.api.{BuiltInComponents, Configuration, mvc}
import pureconfig.ConfigConvert.{catchReadError, viaNonEmptyString}
import pureconfig.{ConfigConvert, loadConfigOrThrow}

import io.scalac.bootstrap.config.SilhouetteConfig
import io.scalac.common.auth.{BearerTokenEnv, CustomSecuredErrorHandler, CustomUnsecuredErrorHandler}
import io.scalac.services.auth.{AuthUserService, DefaultAuthorizationService, DelegablePasswordInfoDao}

trait SilhouetteComponents
  extends EhCacheComponents
    with SecuredActionComponents
    with UnsecuredActionComponents
    with UserAwareActionComponents
    with SecuredErrorHandlerComponents
    with UnsecuredErrorHandlerComponents {
  self: DatabaseComponents
    with ServicesComponents
    with ExecutionComponents
    with BuiltInComponents =>

  override lazy val securedErrorHandler: SecuredErrorHandler = new CustomSecuredErrorHandler()
  override lazy val unsecuredErrorHandler: UnsecuredErrorHandler = new CustomUnsecuredErrorHandler()

  val silhouetteDefaultBodyParser = new mvc.BodyParsers.Default(playBodyParsers)

  override def securedBodyParser: BodyParsers.Default = silhouetteDefaultBodyParser
  override def unsecuredBodyParser: BodyParsers.Default = silhouetteDefaultBodyParser
  override def userAwareBodyParser: BodyParsers.Default = silhouetteDefaultBodyParser

  val silhouetteConfig = loadConfigOrThrow[SilhouetteConfig](configuration.underlying.getConfig("silhouette"))

  val cacheLayer = new PlayCacheLayer(defaultCacheApi)
  val idGenerator = new SecureRandomIDGenerator()
  val passwordHasher = new BCryptPasswordHasher
  val passwordHasherRegistry = PasswordHasherRegistry(passwordHasher)
  val fingerprintGenerator = new DefaultFingerprintGenerator(includeRemoteAddress = false)
  val silhouetteEventBus = new EventBus()
  val silhouetteClock = Clock()

  val authInfoRepository = {
    //Here add authInfoDao instances for any authorization provided we use like PasswordInfo,
    //OAuth2Info (for GitHub authentication) etc
    val passwordInfoDao = new DelegablePasswordInfoDao(authProviderRepo, passInfoRepo, appClock, dbExecutor)

    new DelegableAuthInfoRepository(passwordInfoDao)
  }

  val credentialsProvider = new CredentialsProvider(authInfoRepository, passwordHasherRegistry)

  val silhouette = {
    val authenticatorService = provideAuthenticatorService(idGenerator, cacheLayer, configuration, silhouetteClock)
    val env = provideEnvironment(authUsersService, authenticatorService, silhouetteEventBus)
    new SilhouetteProvider[BearerTokenEnv](env, securedAction, unsecuredAction, userAwareAction)
  }

  val signUpService = new DefaultAuthorizationService(authUsersService, authInfoRepository, passwordHasherRegistry,
    authTokenService, credentialsProvider, silhouette, silhouetteConfig, appClock)

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

    //TODO cached, shouldn't be a problem even for multiple app instances... OR! for each instance new token might be generated
    //Maybe adapt `AuthTokenService` to switch cached version
    //https://www.silhouette.rocks/v5.0/docs/authenticator
    val authenticatorRepository = new CacheAuthenticatorRepository[BearerTokenAuthenticator](cacheLayer)

    new BearerTokenAuthenticatorService(config, authenticatorRepository, idGenerator, clock)
  }
}
