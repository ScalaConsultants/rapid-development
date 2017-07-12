package io.scalac.domain.entities

import java.util.UUID

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.repository.VersionedRepository
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.basic.DatabaseConfig

import io.scalac.common.db.PostgresJdbcProfile

final case class MerchantBillingSettings(
  override val id: Option[UUID],
  merchantId: UUID,
  language: String,
  paymentType: PaymentTypes.Value,
  commissionType: CommissionTypes.Value,
  netDays: NetDays.Value,
  defaultCommission: BigDecimal,
  tags: List[String],
  contractStartDate: DateTime,
  contractEndDate: DateTime,
  cycleType: CycleTypes.Value,
  cycleStartDate: Option[DateTime],
  override val version: Option[Int]
) extends VersionedEntity[MerchantBillingSettings, UUID, Int] {

  override def withId(id: UUID): MerchantBillingSettings = this.copy(id = Some(id))
  override def withVersion(version: Int): MerchantBillingSettings = this.copy(version = Some(version))
}

class MerchantBillingSettingsSlickPostgresRepository (
  val dbConfig: DatabaseConfig[PostgresJdbcProfile]
) extends VersionedRepository[MerchantBillingSettings, UUID, Int](dbConfig.profile) with MerchantCommonMappers {

  import dbConfig.profile.api._
  override type TableType = MerchantBillingSettingsTable
  override val versionType = implicitly[BaseTypedType[Int]]
  override val pkType = implicitly[BaseTypedType[UUID]]
  override val tableQuery = TableQuery[MerchantBillingSettingsTable]

  class MerchantBillingSettingsTable(tag: slick.lifted.Tag) extends Table[MerchantBillingSettings](tag, "merchant_billing_settings") with Versioned[UUID, Int] {
    override def id = column[UUID]("id", O.PrimaryKey)
    def merchantId = column[UUID]("merchant_id", O.Unique)
    def language = column[String]("language", O.SqlType("VARCHAR(50)"))
    def paymentType = column[PaymentTypes.Value]("payment_types", O.SqlType("enum"))
    def commissionType = column[CommissionTypes.Value]("commission_types", O.SqlType("enum"))
    def netDays = column[NetDays.Value]("net_days", O.SqlType("enum"))
    def defaultCommission = column[BigDecimal]("default_commission")
    def tags = column[List[String]]("tags", O.Default(List.empty[String]))
    def contractStartDate = column[DateTime]("contract_start_date", O.SqlType("timestampz"))
    def contractEndDate = column[DateTime]("contract_end_date", O.SqlType("timestampz"))
    def cycleType = column[CycleTypes.Value]("cycle_type", O.SqlType("enum"))
    def cycleStartDate = column[DateTime]("cycle_start_date", O.SqlType("timestampz"))
    override def version = column[Int]("version")

//    def merchant = foreignKey("merchant_id", merchantId, MerchantsSlickPostgresRepository.tableQuery)(_.id, onUpdate=ForeignKeyAction.Restrict)

    def * = (id.?, merchantId, language, paymentType, commissionType, netDays, defaultCommission, tags, contractStartDate,
      contractEndDate, cycleType, cycleStartDate.?, version.?) <> ((MerchantBillingSettings.apply _).tupled, MerchantBillingSettings.unapply)
  }
}
