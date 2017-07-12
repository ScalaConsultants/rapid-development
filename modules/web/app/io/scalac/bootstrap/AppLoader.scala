package io.scalac.bootstrap

import com.typesafe.config.ConfigFactory
import filters.{ExampleFilter, Filters}
import io.scalac.common.controllers.HealthCheckController
import io.scalac.common.core.Correlation
import io.scalac.common.logger.Logging
import io.scalac.common.play.{RootHttpErrorHandler, RootRequestHandler}
import io.scalac.common.services.{DatabaseHealthCheck, HealthCheckServicesImpl, NoopServiceProfiler}
import io.scalac.controllers.MerchantsController
import io.scalac.common.db.{DBExecutor, PostgresJdbcProfile}
import io.scalac.domain.dao.SlickMerchantsDao
import io.scalac.domain.entities.MerchantSlickPostgresRepository
import io.scalac.domain.services.DefaultMerchantsService
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService
import play.api.ApplicationLoader.Context
import play.api._
import play.api.http.{HttpErrorHandler, HttpRequestHandler}
import play.api.inject.ApplicationLifecycle
import play.api.mvc.ControllerComponents
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import slick.basic.DatabaseConfig

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class AppLoader extends ApplicationLoader {
  def load(context: Context) = {
    new MyComponents(context).application
  }
}

class MyComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with _root_.controllers.AssetsComponents
    with Logging {

  implicit val _controllerComponents: ControllerComponents = controllerComponents

  val defaultScheduler: Scheduler = monix.execution.Scheduler.Implicits.global
  val databaseScheduler: SchedulerService = Scheduler.io(name="database")
  implicit val serviceProfiler = NoopServiceProfiler
  val dbConfig = providePostgresDatabaseProfile(applicationLifecycle, databaseScheduler)
  val dbExecutor = new DBExecutor(dbConfig, databaseScheduler)

  val merchantsRepo = new MerchantSlickPostgresRepository(dbConfig)
  val merchantsDao = new SlickMerchantsDao(merchantsRepo, dbExecutor)

  val merchantsService = new DefaultMerchantsService(merchantsDao)

  val healthCheckController = {
    val dbHealthCheck = new DatabaseHealthCheck(dbConfig)
    val externalHealthChecks = List(dbHealthCheck)
    val config = ConfigFactory.load("build-info")
    val services = new HealthCheckServicesImpl(externalHealthChecks, config)

    new HealthCheckController(services, defaultScheduler)
  }

  val merchantsController = new MerchantsController(merchantsService, defaultScheduler)

  override lazy val httpErrorHandler: HttpErrorHandler = {
    //router below is used only in dev mode, causes stack overflow for Some(router) anyway
    new RootHttpErrorHandler(environment, configuration, sourceMapper, router = None)
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
      merchantsController)
  }

  private def providePostgresDatabaseProfile(lifecycle: ApplicationLifecycle,
    scheduler: Scheduler): DatabaseConfig[PostgresJdbcProfile] = {
    implicit val ex = scheduler

    val env = Option(System.getProperty("env")).getOrElse("dev")
    val dbConf = env match {
      case "dev" => DatabaseConfig.forConfig[PostgresJdbcProfile]("postgres_dev")
      case x => DatabaseConfig.forConfig[PostgresJdbcProfile](x)
    }
    lifecycle.addStopHook { () =>
      implicit val c = Correlation.withNotRequired
      Future {
        Try(dbConf.db.close()) match {
          case Success(_) => logger.info("Database successfully closed.")
          case Failure(t) => logger.warn("Error occurred while closing database.", t)
        }
      }
    }
    dbConf
  }

}
