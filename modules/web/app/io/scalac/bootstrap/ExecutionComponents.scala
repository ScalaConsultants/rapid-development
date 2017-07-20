package io.scalac.bootstrap

import monix.execution.schedulers.SchedulerService
import monix.execution.{Scheduler, UncaughtExceptionReporter}
import org.slf4j.LoggerFactory

import io.scalac.common.services.JodaAppClock

trait ExecutionComponents {

  val reporter = UncaughtExceptionReporter { ex =>
    val logger = LoggerFactory.getLogger("monix")
    logger.error("Uncaught exception", ex)
  }

  val defaultScheduler: Scheduler = Scheduler(monix.execution.Scheduler.Implicits.global, reporter)

  val databaseScheduler: SchedulerService = Scheduler.io(name = "database")

  val appClock = new JodaAppClock()
}
