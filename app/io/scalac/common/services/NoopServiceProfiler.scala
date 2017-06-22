package io.scalac.common.services

import com.google.inject.Singleton

@Singleton
class NoopServiceProfiler extends ServiceProfiler {

  override def incrementCounter(identifier: String): Unit = ()

  override def recordResponseTime(identifier: String, millis: Long): Unit = ()

  override def recordError(error: Throwable): Unit = ()
}
