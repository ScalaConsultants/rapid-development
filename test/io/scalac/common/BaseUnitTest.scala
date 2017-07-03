package io.scalac.common

import monix.eval.Task
import org.scalatest.{Matchers, WordSpec}

import io.scalac.common.logger.AppLogger
import io.scalac.common.services.{Context, Logger, NoopServiceProfiler, Profiler, Service, ServiceError}

trait BaseUnitTest extends WordSpec with Matchers with FuturesSupport {

  implicit val scheduler = monix.execution.Scheduler.Implicits.global
  implicit val serviceContext: auth.ServiceContext = auth.EmptyContext()
  implicit val serviceProfiler: services.ServiceProfiler = NoopServiceProfiler
  implicit val logger: AppLogger = AppLogger(getClass)

  def mockService[Req, Res, E <: ServiceError](fn: Req => Task[Either[E, Res]]): Service[Req, Res, E] = new Service[Req, Res, E] {
    def apply[Ctx: Context, P: Profiler, L: Logger](req: Req): Task[Either[E, Res]] = fn(req)
  }
}
