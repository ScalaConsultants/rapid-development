package io.scalac.common.services

import org.joda.time.{DateTime, DateTimeZone}

trait AppClock {

  def now: DateTime
}

class JodaAppClock extends AppClock {

  override def now: DateTime =
    DateTime.now(DateTimeZone.UTC)
}
