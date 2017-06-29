import java.time.Clock

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import com.google.inject.name.{Named, Names}
import com.google.inject.{AbstractModule, Provides, Singleton}
import com.typesafe.config.{Config, ConfigFactory}
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService
import play.api.inject.ApplicationLifecycle
import slick.basic.DatabaseConfig

import io.scalac.common.core.Correlation
import io.scalac.common.logger.Logging
import io.scalac.common.services.{HealthCheckServicesImpl, _}
import io.scalac.domain.PostgresJdbcProfile
import io.scalac.domain.dao.{NotesDao, SlickNotesDao}
import io.scalac.services.{DefaultNotesService, NotesService}

class Module extends AbstractModule with Logging {

  override def configure(): Unit = {
    bind(classOf[Clock]).toInstance(Clock.systemUTC())

    val defaultScheduler: Scheduler = monix.execution.Scheduler.Implicits.global
    bind(classOf[Scheduler]).annotatedWith(Names.named("DefaultScheduler")).toInstance(defaultScheduler)
    val databaseScheduler: SchedulerService = Scheduler.io(name="database")
    bind(classOf[Scheduler]).annotatedWith(Names.named("DatabaseScheduler")).toInstance(databaseScheduler)

    bind(classOf[Config]).annotatedWith(Names.named("BuildInfo")).toInstance(ConfigFactory.load("build-info"))

    bind(classOf[ServiceProfiler]).to(classOf[NoopServiceProfiler]).asEagerSingleton()

    bind(classOf[NotesDao]).to(classOf[SlickNotesDao]).asEagerSingleton()

    bind(classOf[NotesService]).to(classOf[DefaultNotesService]).asEagerSingleton()
    bind(classOf[HealthCheckServices]).to(classOf[HealthCheckServicesImpl]).asEagerSingleton()
  }

  @Provides @Singleton
  def providePostgresDatabaseProfile(lifecycle: ApplicationLifecycle,
    @Named("DatabaseScheduler") scheduler: Scheduler): DatabaseConfig[PostgresJdbcProfile] = {
    implicit val ex = scheduler

    val env = Option(System.getProperty("env")).getOrElse("dev")
//    import PostgresJdbcProfile.api._
//    val dbConf = env match {
//      case "dev" => Database.forConfig("postgres_dev")
//      case x => Database.forConfig(x)
//    }
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
