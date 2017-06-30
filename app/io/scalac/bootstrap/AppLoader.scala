package io.scalac.bootstrap

import com.typesafe.config.ConfigFactory
import filters.{ExampleFilter, Filters}
import io.scalac.common.controllers.HealthCheckController
import io.scalac.common.core.Correlation
import io.scalac.common.logger.Logging
import io.scalac.common.play.{RootHttpErrorHandler, RootRequestHandler}
import io.scalac.common.services.{DatabaseHealthCheck, ExternalHealthChecks, HealthCheckServicesImpl, NoopServiceProfiler}
import io.scalac.controllers.NotesController
import io.scalac.domain.PostgresJdbcProfile
import io.scalac.domain.dao.{DBExecutor, SlickNotesDao}
import io.scalac.domain.entities.NotesSlickPostgresRepository
import io.scalac.services.DefaultNotesService
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService
import play.api.ApplicationLoader.Context
import play.api._
import play.api.http.HttpRequestHandler
import play.api.inject.ApplicationLifecycle
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

  val defaultScheduler: Scheduler = monix.execution.Scheduler.Implicits.global
  val databaseScheduler: SchedulerService = Scheduler.io(name="database")
  implicit val serviceProfiler = NoopServiceProfiler
  val dbConfig = providePostgresDatabaseProfile(applicationLifecycle, databaseScheduler)

  lazy val errorHandler = {
    new RootHttpErrorHandler(environment, configuration, sourceMapper, None)
  }

  lazy val healthCheckController = {
    val dbHealthCheck = new DatabaseHealthCheck(dbConfig)
    val externalHealthChecks = new ExternalHealthChecks(dbHealthCheck)
    val config = ConfigFactory.load("build-info")
    val services = new HealthCheckServicesImpl(externalHealthChecks, config)

    new HealthCheckController(controllerComponents, services, defaultScheduler)
  }

  lazy val notesController = {
    val notesRepo = new NotesSlickPostgresRepository(dbConfig)
    val dbExecutor = new DBExecutor(dbConfig, databaseScheduler)
    val notesDao = new SlickNotesDao(notesRepo, dbExecutor)
    val notesService = new DefaultNotesService(notesDao)

    new NotesController(notesService, defaultScheduler)
  }

  lazy val router: Router = {
    new _root_.router.Routes(httpErrorHandler, assets, healthCheckController, notesController)
  }

  override lazy val httpRequestHandler: HttpRequestHandler = {
    val filters = new Filters(environment, new ExampleFilter)
    new RootRequestHandler(httpErrorHandler, httpConfiguration, filters, router)
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