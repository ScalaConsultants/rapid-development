package io.scalac.common.db

import cats.syntax.either._
import io.scalac.common.services.{DatabaseCallFailed, DatabaseError, DatabaseResponse}
import monix.eval.Task
import monix.execution.Scheduler
import slick.basic.DatabaseConfig
import slick.dbio.DBIO

import scala.language.implicitConversions

class DBExecutor (
  dbConfig: DatabaseConfig[PostgresJdbcProfile],
  val scheduler: Scheduler) {

  /**
    * Invokes Slick's DB call. Changes Future into Monix's task and uses
    * separate <i>database</i> Execution Context to execute the call, than goes back
    * to <i>global</i> execution context.
    */
  implicit def executeOperation[T](databaseOperation: DBIO[T]): DatabaseResponse[T] = {
    Task.fork(Task.fromFuture(dbConfig.db.run(databaseOperation)), scheduler)
      .map(_.asRight[DatabaseError])
      .asyncBoundary
      .onErrorHandle { ex =>
        DatabaseCallFailed(ex.getMessage).asLeft[T]
      }
  }
}
