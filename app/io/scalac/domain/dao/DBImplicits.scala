package io.scalac.domain.dao

import scala.language.implicitConversions

import cats.syntax.either._
import com.google.inject.Inject
import com.google.inject.name.Named
import monix.eval.Task
import monix.execution.Scheduler
import slick.basic.DatabaseConfig
import slick.dbio.DBIO

import io.scalac.common.services.{DBResponse, DatabaseCallFailed, DatabaseError}
import io.scalac.domain.PostgresJdbcProfile

class DBImplicits @Inject()(
  dbConfig: DatabaseConfig[PostgresJdbcProfile],
  @Named("DatabaseScheduler") scheduler: Scheduler) {

  implicit val dbEx = scheduler

  /**
    * Invokes Slick's DB call. Changes Future into Monix's task and uses
    * separate <i>database</i> Execution Context to execute the call, than goes back
    * to <i>global</i> execution context.
    */
  implicit def executeOperation[T](databaseOperation: DBIO[T]): DBResponse[T] = {
    Task.fork(Task.fromFuture(dbConfig.db.run(databaseOperation)), dbEx)
      .map(_.asRight[DatabaseError])
      .asyncBoundary
      .onErrorHandle { ex =>
        DatabaseCallFailed(ex.getMessage).asLeft[T]
      }
  }
}
