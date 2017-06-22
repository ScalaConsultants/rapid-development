package io.scalac.common

import org.scalatest.{Matchers, WordSpec}

import io.scalac.common.logger.AppLogger
import io.scalac.common.services.NoopServiceProfiler

trait BaseUnitTest extends WordSpec with Matchers with FuturesSupport {

  implicit val serviceContext: auth.ServiceContext = auth.EmptyContext()
  implicit val serviceProfiler: services.ServiceProfiler = new NoopServiceProfiler()
  implicit val logger: AppLogger = AppLogger(getClass)
}
