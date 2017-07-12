package io.scalac.domain.entities

import io.scalac.common.db.PostgresJdbcProfile.api._

trait MerchantCommonMappers {

  implicit val paymentTypesMapper = MappedColumnType.base[PaymentTypes.Value, String](
    _.toString, PaymentTypes.withName
  )

  implicit val commissionTypesMapper = MappedColumnType.base[CommissionTypes.Value, String](
    _.toString, CommissionTypes.withName
  )

  implicit val netDaysMapper = MappedColumnType.base[NetDays.Value, String](
    _.toString, NetDays.withName
  )

  implicit val cycleTypesMapper = MappedColumnType.base[CycleTypes.Value, String](
    _.toString, CycleTypes.withName
  )
}
