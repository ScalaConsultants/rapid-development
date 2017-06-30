package io.scalac.common.services

object NoopServiceProfiler extends ServiceProfiler {

  override def incrementCounter(identifier: String): Unit = ()

  override def recordResponseTime(identifier: String, millis: Long): Unit = ()

  override def recordError(error: Throwable): Unit = ()
}
