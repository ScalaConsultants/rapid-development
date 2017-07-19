package io.scalac.common.db

import scala.concurrent.Future

import cats.syntax.either._

import io.scalac.common.services.{DatabaseCallFailed, DatabaseError, DatabaseResponse}
import monix.eval.Task
import monix.execution.Scheduler
import slick.basic.DatabaseConfig
import scala.language.implicitConversions

class DBExecutor(
  val dbConfig: DatabaseConfig[PostgresJdbcProfile],
  val scheduler: Scheduler) {

  import dbConfig.profile.api._

  /**
    * Invokes Slick's DB call. Changes Future into Monix's task and uses
    * separate <i>database</i> Execution Context to execute the call, than goes back
    * to <i>global</i> execution context.
    */
  implicit def executeOperation[T](databaseOperation: DBIO[T]): DatabaseResponse[T] = {
    Task.fork(Task.deferFuture(dbConfig.db.run(databaseOperation)), scheduler)
      .map(_.asRight[DatabaseError])
      .asyncBoundary
      .onErrorHandle { ex =>
        DatabaseCallFailed(ex.getMessage).asLeft[T]
      }
  }

  /**
    * Invokes Slick's DB calls transactionally. Changes Future into Monix's task and uses
    * separate <i>database</i> Execution Context to execute the call, than goes back
    * to <i>global</i> execution context.
    */
  def executeTransactionally[T](databaseOperation: DBIO[T]): DatabaseResponse[T] = {
    Task.fork(Task.deferFuture(dbConfig.db.run(databaseOperation.transactionally)), scheduler)
      .map(_.asRight[DatabaseError])
      .asyncBoundary
      .onErrorHandle { ex =>
        DatabaseCallFailed(ex.getMessage).asLeft[T]
      }
  }

  def evalFuture[T](databaseOperation: DBIO[T]): Future[T] = {
      dbConfig.db.run(databaseOperation)
    }
}
