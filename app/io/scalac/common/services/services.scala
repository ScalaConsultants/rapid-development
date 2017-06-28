package io.scalac.common

import monix.eval.Task
import monix.execution.misc.NonFatal

import io.scalac.common.auth.ServiceContext
import io.scalac.common.logger.AppLogger

package object services {

  sealed trait Error extends Product with Serializable

  //TODO should they all contain msg: String?
  sealed trait ServiceError extends Error
  sealed trait ExternalServiceError extends Error
  sealed trait DatabaseError extends Error

  final case class ServiceFailed(msg: String) extends ServiceError
  final case class EmptyResponse(msg: String) extends ServiceError
  final case class DatabaseCallFailed(msg: String) extends DatabaseError
  final case class ResourceNotFound(msg: String) extends DatabaseError
//  final case class ExternalServiceValidationError(endpoint: String, parseErrors: Seq[(JsPath, scala.Seq[ValidationError])])
//    extends ExternalServiceError

  trait ServiceProfiler {

    def incrementCounter(identifier: String): Unit
    def recordResponseTime(identifier: String, millis: Long): Unit
    def recordError(error: Throwable): Unit
  }

  type Profiler[_] = ServiceProfiler
  type Context[_] = ServiceContext
  type Logger[_] = AppLogger

  type Response[R] = Task[Either[ServiceError, R]] //TODO or () => ...
  type ExternalResponse[R] = Task[Either[ExternalServiceError, R]]
  type DBResponse[R] = Task[Either[DatabaseError, R]]

  implicit class DbResponseImplicits[T](dBResponse: DBResponse[T]) {

    import cats.syntax.either._

    val toServiceResponse: Task[Either[ServiceError, T]] = {
      dBResponse.map(_.leftMap {
        case DatabaseCallFailed(msg) =>
          ServiceFailed(msg)
        case ResourceNotFound(msg) =>
          EmptyResponse(msg)
      })
    }
  }

  trait Service[-Req, +Res, E <: Error] {

    def apply[Ctx: Context, P: Profiler, L: Logger](req: Req): Task[Either[E, Res]]
  }

  object Service {

    def apply[Req, Res, E <: Error](serviceId: String)
                                   (block: Req => ServiceContext => Task[Either[E, Res]]): Service[Req, Res, E] =
      new Service[Req, Res, E] {
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
