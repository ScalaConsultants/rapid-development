package io.scalac.bootstrap

import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService

import io.scalac.common.services.JodaAppClock

trait ExecutionComponents {

  val defaultScheduler: Scheduler = monix.execution.Scheduler.Implicits.global

  val databaseScheduler: SchedulerService = Scheduler.io(name = "database")

  val appClock = new JodaAppClock()
}
