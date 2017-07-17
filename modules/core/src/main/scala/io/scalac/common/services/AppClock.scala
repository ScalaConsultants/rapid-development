package io.scalac.common.services

import org.joda.time.{DateTime, DateTimeZone}

trait AppClock {

  def now: DateTime
}

object AppClock {

  def apply(dateTimeZone: DateTimeZone) = new AppClock {
    def now = DateTime.now(dateTimeZone)
  }
}
