package io.scalac.domain.entities

import java.util.UUID

import com.byteslounge.slickrepo.meta.VersionedEntity
import org.joda.time.DateTime

final case class MerchantInvoices(
  override val id: Option[UUID],
  merchantId: UUID,//TODO foreign key to Merchant
  lastInvoiceData: Option[DateTime],
  unbilledBookings: Option[Int],
  balance: Option[BigDecimal],
  override val version: Option[Int]
) extends VersionedEntity[MerchantInvoices, UUID, Int] {

  override def withId(id: UUID): MerchantInvoices = this.copy(id = Some(id))
  override def withVersion(version: Int): MerchantInvoices = this.copy(version = Some(version))
}
