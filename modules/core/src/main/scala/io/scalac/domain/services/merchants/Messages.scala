package io.scalac.domain.services.merchants

import java.util.UUID

import org.joda.time.DateTime

import io.scalac.common.entities.Pagination
import io.scalac.domain.entities.{CommissionTypes, CycleTypes, PaymentTypes}

final case class ListMerchantSearchCriteria(
  active: Boolean,
  billingCycle: CycleTypes.Value,
  paymentType: PaymentTypes.Value,
  commissionType: CommissionTypes.Value,
  lastInvoiceDate: Option[DateTime],
  pagination: Pagination
)

final case class OutgoingMerchant(
  id: UUID,
  merchantNo: Int,
  name: String,
  city: String,
  country: String,
  active: Boolean,
  lastInvoiceData: Option[DateTime],
  unbilledBookings: Option[Int],
  balance: Option[BigDecimal]
)
