package io.scalac.domain.services.merchants

import io.scalac.domain.entities.{Merchant, MerchantInvoices}

object Conversions {

  def toOutgoingMerchant(merchant: Merchant, merchantInvoices: MerchantInvoices) =
    OutgoingMerchant(
      merchant.id.get,
      merchant.merchantNo,
      merchant.name,
      merchant.city,
      merchant.country,
      merchant.active,
      merchantInvoices.lastInvoiceDate,
      merchantInvoices.unbilledBookings,
      merchantInvoices.balance
    )
}
