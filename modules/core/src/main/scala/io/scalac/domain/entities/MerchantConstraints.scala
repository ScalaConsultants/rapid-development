package io.scalac.domain.entities

object PaymentTypes extends Enumeration {

  val Prepaid, Postpaid, Pending = Value
}

object CommissionTypes extends Enumeration {

  val Fixed, Vary = Value
}

object NetDays extends Enumeration {

  val PerCover, PerBooking = Value
}

object CycleTypes extends Enumeration {

  val EndOfMonth, ExactDate = Value
}
