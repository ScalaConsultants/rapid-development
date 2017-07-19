package io.scalac.bootstrap

import com.typesafe.config.ConfigFactory
import filters.{ExampleFilter, Filters}
import play.api.ApplicationLoader.Context
import play.api._
import play.api.http.{HttpErrorHandler, HttpRequestHandler}
import play.api.mvc.ControllerComponents
import play.api.routing.Router
import play.filters.HttpFiltersComponents

import io.scalac.common.controllers.HealthCheckController
import io.scalac.common.play.{RootHttpErrorHandler, RootRequestHandler}
import io.scalac.common.services.{DatabaseHealthCheck, HealthCheckServicesImpl}
import io.scalac.controllers.auth.AuthorizationController
import io.scalac.controllers.{NotesController, PagesController}

class AppLoader extends ApplicationLoader {

  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    new PlayComponents(context).application
  }
}

class PlayComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with _root_.controllers.AssetsComponents
    with ExecutionComponents
    with DatabaseComponents
    with ServicesComponents
    with SilhouetteComponents {

  implicit val _controllerComponents: ControllerComponents = controllerComponents

  val healthCheckController = {
    val dbHealthCheck = new DatabaseHealthCheck(dbConfig)
    val externalHealthChecks = List(dbHealthCheck)
    val config = ConfigFactory.load("build-info")
    val services = new HealthCheckServicesImpl(externalHealthChecks, config)
    new HealthCheckController(services, defaultScheduler)
  }

  val notesController = new NotesController(notesService, defaultScheduler)
  val pagesController = new PagesController(notesService, assetsFinder, defaultScheduler)

  val signUpController = new AuthorizationController(silhouette, signUpService, defaultScheduler)

  override lazy val httpErrorHandler: HttpErrorHandler = {
    //router below is used only in dev mode, causes stack overflow for Some(router) anyway
    new RootHttpErrorHandler(environment, configuration, sourceMapper, router = None, assetsFinder)
  }
  override lazy val httpRequestHandler: HttpRequestHandler = {
    val filters = new Filters(environment, new ExampleFilter)
    new RootRequestHandler(httpErrorHandler, httpConfiguration, filters, router)
  }
  override lazy val router: Router = {
    new _root_.router.Routes(
      httpErrorHandler,
      assets,
      healthCheckController,
      pagesController,
      signUpController,
      notesController)
  }
}
