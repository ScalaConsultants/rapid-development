package io.scalac.domain.entities

import java.util.UUID

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.repository.VersionedRepository
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.basic.DatabaseConfig

import io.scalac.common.db.PostgresJdbcProfile

final case class MerchantInvoices(
  override val id: Option[UUID],
  merchantId: UUID,
  lastInvoiceDate: Option[DateTime],
  unbilledBookings: Option[Int],
  balance: Option[BigDecimal],
  override val version: Option[Int]
) extends VersionedEntity[MerchantInvoices, UUID, Int] {

  override def withId(id: UUID): MerchantInvoices = this.copy(id = Some(id))
  override def withVersion(version: Int): MerchantInvoices = this.copy(version = Some(version))
}

class MerchantInvoicesSlickPostgresRepository (
  val dbConfig: DatabaseConfig[PostgresJdbcProfile]
) extends VersionedRepository[MerchantInvoices, UUID, Int](dbConfig.profile) {

  import dbConfig.profile.api._
  override type TableType = MerchantInvoicesTable
  override val versionType = implicitly[BaseTypedType[Int]]
  override val pkType = implicitly[BaseTypedType[UUID]]
  override val tableQuery = TableQuery[MerchantInvoicesTable]

  class MerchantInvoicesTable(tag: slick.lifted.Tag) extends Table[MerchantInvoices](tag, "merchant_contact_information") with Versioned[UUID, Int] {
    override def id = column[UUID]("id", O.PrimaryKey)
    def merchantId = column[UUID]("merchant_id", O.Unique)
    def lastInvoiceDate = column[DateTime]("last_invoice_date", O.SqlType("timestampz"))
    def unbilledBookings = column[Int]("unbilled_bookings")
    def balance = column[BigDecimal]("balance")
    override def version = column[Int]("version")

    //    def merchant = foreignKey("merchant_id", merchantId, MerchantsSlickPostgresRepository.tableQuery)(_.id, onUpdate=ForeignKeyAction.Restrict)

    def * = (id.?, merchantId, lastInvoiceDate.?, unbilledBookings.?, balance.?, version.?) <> (
      (MerchantInvoices.apply _).tupled, MerchantInvoices.unapply)
  }
}
