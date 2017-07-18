package io.scalac.bootstrap

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import monix.execution.Scheduler
import play.api.BuiltInComponents
import play.api.inject.ApplicationLifecycle
import slick.basic.DatabaseConfig

import io.scalac.common.core.Correlation
import io.scalac.common.db.{DBExecutor, PostgresJdbcProfile}
import io.scalac.common.logger.Logging
import io.scalac.domain.dao.{SlickAuthTokenDao, SlickAuthUsersDao, SlickNotesDao, SlickUsersDao}
import io.scalac.domain.entities._

trait DatabaseComponents extends Logging {
  self: BuiltInComponents
    with ExecutionComponents =>

  val dbConfig = providePostgresDatabaseProfile(applicationLifecycle, databaseScheduler)
  val dbExecutor = new DBExecutor(dbConfig, databaseScheduler)

  val usersRepo = new UsersSlickPostgresRepository(dbConfig)
  val notesRepo = new NotesSlickPostgresRepository(dbConfig)
  val authUsersRepo = new AuthenticationProvidersSlickPostgresRepository(dbConfig)
  val tokensRepo = new TokensSlickPostgresRepository(dbConfig)
  val authProviderRepo = new AuthenticationProvidersSlickPostgresRepository(dbConfig)
  val passInfoRepo = new PasswordInformationSlickPostgresRepository(dbConfig)

  val usersDao = new SlickUsersDao(usersRepo, dbExecutor)
  val authUsersDao = new SlickAuthUsersDao(usersRepo, authUsersRepo, dbExecutor)
  val authTokenDao = new SlickAuthTokenDao(tokensRepo, usersRepo, appClock, dbExecutor)
  val notesDao = new SlickNotesDao(notesRepo, dbExecutor)

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
