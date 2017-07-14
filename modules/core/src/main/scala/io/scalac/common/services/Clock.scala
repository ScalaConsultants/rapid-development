package io.scalac.common.services

import org.joda.time.{DateTime, DateTimeZone}

trait Clock {

  def now: DateTime
}

object Clock {

  def apply(dateTimeZone: DateTimeZone) = new Clock {
    def now = DateTime.now(dateTimeZone)
  }
}
