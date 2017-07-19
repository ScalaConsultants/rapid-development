package io.scalac.common

import scala.concurrent.Future

import io.scalac.common.logger.AppLogger
import monix.eval.Task
import monix.execution.misc.NonFatal

import io.scalac.common.core.Correlation

package object services {

  sealed trait Error extends Product with Serializable

  //TODO still not sure if it actually makes sense
  sealed trait ServiceError extends Error
  sealed trait ExternalServiceError extends Error
  sealed trait DatabaseError extends Error
  sealed trait AuthError extends Error

  final case class ServiceFailed(msg: String) extends ServiceError
  final case class MissingResource(msg: String) extends ServiceError
  final case class InvalidResource(msg: Seq[String]) extends ServiceError

  final case class AuthFailed(msg: String) extends AuthError
  final case object UserAlreadyExists extends AuthError
  final case object UserNotFound extends AuthError
  final case object UserUnauthorized extends AuthError

  final case class DatabaseCallFailed(msg: String) extends DatabaseError
  final case class ResourceNotFound(msg: String) extends DatabaseError

  //  final case class ExternalServiceValidationError(endpoint: String, parseErrors: Seq[(JsPath, scala.Seq[ValidationError])])
//    extends ExternalServiceError

  trait ServiceProfiler {

    def incrementCounter(identifier: String): Unit
    def recordResponseTime(identifier: String, millis: Long): Unit
    def recordError(error: Throwable): Unit
  }

  //authenticated user
  trait Subject

  //user on behalf subject is working, a subject is ordinary user or admin
  trait Delegate

  final case class ServiceContext(
    correlation: Correlation,
    properties: Map[String, String],
    subject: Option[Subject],
    delegate: Option[Delegate]
  )

  def EmptyContext(c: Correlation = Correlation.withNew) = ServiceContext(
    correlation = c,
    properties = Map.empty,
    subject = None,
    delegate = None
  )

  type Profiler[_] = ServiceProfiler
  type Context[_] = ServiceContext
  type Logger[_] = AppLogger

  type ServiceResponse[R] = Task[Either[ServiceError, R]]
  type ExternalResponse[R] = Task[Either[ExternalServiceError, R]]
  type DatabaseResponse[R] = Task[Either[DatabaseError, R]]

  implicit class DbResponseImplicits[T](dBResponse: DatabaseResponse[T]) {

    import cats.syntax.either._

    val toServiceResponse: Task[Either[ServiceError, T]] = {
      dBResponse.map(_.leftMap {
        case DatabaseCallFailed(msg) =>
          ServiceFailed(msg)
        case ResourceNotFound(msg) =>
          MissingResource(msg)
      })
    }

    val toAuthResponse: Task[Either[AuthError, T]] = {
      dBResponse.map(_.leftMap {
        case DatabaseCallFailed(msg) =>
          AuthFailed(msg)
        case _:ResourceNotFound =>
          UserUnauthorized
      })
    }
  }

  implicit class ServiceResponseImplicits[T](response: ServiceResponse[T]) {

    import cats.syntax.either._

    val toAuthResponse: Task[Either[AuthError, T]] = {
      response.map(_.leftMap {
        case ServiceFailed(msg) =>
          AuthFailed(msg)
        case _ =>
          AuthFailed("Failed to process authentication or authorization")
      })
    }
  }

  implicit class FutureToServiceImplicits[T](async: Future[T]) {

    import cats.syntax.either._

    def asServiceCall = {
      Task.deferFutureAction(implicit scheduler => async)
        .map(_.asRight[ServiceError])
        .onErrorHandle { ex =>
          ServiceFailed(ex.getMessage).asLeft[T]
        }
    }

    def asAuthCall = {
      Task.deferFutureAction(implicit scheduler => async)
        .map(_.asRight[AuthError])
        .onErrorHandle { ex =>
          AuthFailed(ex.getMessage).asLeft[T]
        }
    }
  }

  trait Service[-Req, +Res, E <: Error] {

    def apply[Ctx: Context, P: Profiler, L: Logger](req: Req): Task[Either[E, Res]]
  }

  object Service {

    def apply[Req, Res, E <: Error](serviceId: String)
                                   (block: Req => ServiceContext => Task[Either[E, Res]]): Service[Req, Res, E] =
      new Service[Req, Res, E] {
        //TODO logger and profiler should not be passed here, they are not needed for subsequent service invocations
        override def apply[Ctx: Context, P: Profiler, L: Logger](req: Req): Task[Either[E, Res]] = {
          val context = implicitly[Context[_]]
          val profiler = implicitly[Profiler[_]]
          val logger = implicitly[Logger[_]]
          implicit val c = context.correlation

          val id = s"[Service/$serviceId]"
          profiler.incrementCounter(id)
          val startTime = System.currentTimeMillis()
          logger.trace(s"$serviceId - [$req]")
          block(req)(context)
            .map { response =>
              profiler.recordResponseTime(id, System.currentTimeMillis() - startTime)
              response.fold(serviceError => {
                logger.trace(s"$serviceId - service error - [$serviceError]")
              }, serviceResponse => {
                logger.trace(s"$serviceId - successful response - [$serviceResponse]")
              })
              response
            }.onErrorRecoverWith {
            case NonFatal(ex) =>
              logger.error(s"$serviceId - Unhandled error - [$ex]")
              profiler.recordError(ex)
              throw ex
          }
        }
      }
  }

}
