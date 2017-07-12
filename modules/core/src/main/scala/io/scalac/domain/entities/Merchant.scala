package io.scalac.domain.entities

import java.util.UUID

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.repository.VersionedRepository
import com.github.tminglei.slickpg.PgEnumSupport
import io.scalac.common.db.PostgresJdbcProfile
import io.scalac.domain.entities.CommissionType.CommissionType
import io.scalac.domain.entities.DefaultBillingLanguage.DefaultBillingLanguage
import io.scalac.domain.entities.NetDays.NetDays
import io.scalac.domain.entities.PaymentType.PaymentType
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

// assumption: I ignored BillingStatementCycle and Tags
case class Merchant (
  override val id: Option[UUID],
  contractFrom: DateTime,
  contractUntil: DateTime,
  defaultBillingLanguage: DefaultBillingLanguage,
  paymentType: PaymentType,
  commissionType: CommissionType,
  netDays: NetDays,
  merchantInfo: MerchantInfo,
  override val version: Option[Int]
) extends VersionedEntity[Merchant, UUID, Int] {

  // TODO: extract those to some common trait - the same code is in `City` class
  override def withVersion(version: Int): Merchant  = copy(version = Some(version))
  override def withId(id: UUID): Merchant           = copy(id = Some(id))
}

object Merchant {
  def fromDbRepr(id: Option[UUID],
                 contractFrom: DateTime,
                 contractUntil: DateTime,
                 defaultBillingLanguage: DefaultBillingLanguage,
                 paymentType: PaymentType,
                 commissionType: CommissionType,
                 netDays: NetDays,
                 ownerName: Option[String],
                 companyName: Option[String],
                 virtualBankAccount: Option[String],
                 phone: Option[String],
                 email: Option[String],
                 taxId: Option[String],
                 additionalInfo: Option[String],
                 version: Option[Int]): Merchant = {
    val merchantInfo = MerchantInfo(ownerName, companyName, virtualBankAccount, phone, email, taxId, additionalInfo)
    Merchant(id, contractFrom, contractUntil, defaultBillingLanguage, paymentType, commissionType, netDays, merchantInfo, version)
  }

  def toDbRepr(merchant: Merchant): Option[(Option[UUID], DateTime, DateTime, DefaultBillingLanguage, PaymentType, CommissionType, NetDays, Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[Int])] = {
    import shapeless._
    import ops.tuple.FlatMapper
    import syntax.std.tuple._
    import test._

    trait LowPriorityFlatten extends Poly1 {
      implicit def default[T] = at[T](Tuple1(_))
    }
    object flatten extends LowPriorityFlatten {
      implicit def caseTuple[P <: Product](implicit lfm: Lazy[FlatMapper[P, flatten.type]]) =
        at[P](lfm.value(_))
    }

    val merchantInfoTuple = {
      val info = merchant.merchantInfo

      (info.ownerName, info.companyName, info.virtualBankAccount, info.phone, info.email, info.taxId, info.additionalInfo)
    }
    val l = (merchant.id, merchant.contractFrom, merchant.contractUntil, merchant.defaultBillingLanguage, merchant.paymentType, merchant.commissionType, merchant.netDays, merchantInfoTuple, merchant.version)
    val tmp = flatten(l)
    Some(tmp)
  }

}

case class MerchantInfo (
  ownerName: Option[String],
  companyName: Option[String],
  virtualBankAccount: Option[String],
  phone: Option[String],
  email: Option[String],
  taxId: Option[String],
  additionalInfo: Option[String]
)

object DefaultBillingLanguage extends Enumeration {
  type DefaultBillingLanguage = Value

  val Thai    = Value("Thai")
  val Bahasa  = Value("Bahasa")
  val English = Value("English")
  val Chinese = Value("Chinese")
}

object PaymentType extends Enumeration {
  type PaymentType = Value

  val Prepaid  = Value("Prepaid")
  val PostPaid = Value("PostPaid")
}

object CommissionType extends Enumeration {
  type CommissionType = Value

  val Prepaid  = Value("Fixed")
  val PostPaid = Value("Vary")
}

object NetDays extends Enumeration {
  type NetDays = Value

  val PerCover    = Value("PerCover")
  val PerBooking  = Value("PerBooking")
}


object EnumSupport extends PostgresProfile with PgEnumSupport {
  // https://stackoverflow.com/questions/22945485/how-to-map-postgresql-custom-enum-column-with-slick2-0-1

  object EnumsImplicits {
    implicit val defaultBillingLangMapper = createEnumJdbcType("DefaultBillingLanguage_Enum", DefaultBillingLanguage)
    implicit val paymentTypeMapper        = createEnumJdbcType("PaymentType_Enum", PaymentType)
    implicit val commissionTypeMapper     = createEnumJdbcType("CommissionType_Enum", CommissionType)
    implicit val netDaysMapper            = createEnumJdbcType("NetDays_Enum", NetDays)
  }
}

class MerchantSlickPostgresRepository (
  val dbConfig: DatabaseConfig[PostgresJdbcProfile]//TODO or JdbcProfile and import PostgresJdbcProfile.api._ below? Safe?
) extends VersionedRepository[Merchant, UUID, Int](dbConfig.profile) {

  import EnumSupport.EnumsImplicits._
  import dbConfig.profile.api._
  override type TableType = Merchants
  override val versionType = implicitly[BaseTypedType[Int]]
  override def pkType = implicitly[BaseTypedType[UUID]]
  override def tableQuery = TableQuery[Merchants]

  class Merchants(tag: slick.lifted.Tag) extends Table[Merchant](tag, "merchants") with Versioned[UUID, Int] {
    override def id            = column[UUID]("id", O.PrimaryKey)
    def contractFrom           = column[DateTime]("contract_from")
    def contractUntil          = column[DateTime]("contract_until")
    def defaultBillingLanguage = column[DefaultBillingLanguage]("default_billing_language")
    def paymentType            = column[PaymentType]("payment_type")
    def commissionType         = column[CommissionType]("commission_type")
    def netDays                = column[NetDays]("net_days")
    def ownerName              = column[String]("ownerName")
    def companyName            = column[String]("companyName")
    def virtualBankAccount     = column[String]("virtualBankAccount")
    def phone                  = column[String]("phone")
    def email                  = column[String]("email")
    def taxId                  = column[String]("taxId")
    def additionalInfo         = column[String]("additionalInfo")
    override def version       = column[Int]("version")

    def * =
      (id.?, contractFrom, contractUntil, defaultBillingLanguage, paymentType, commissionType, netDays, ownerName.?,
      companyName.?, virtualBankAccount.?, phone.?, email.?, taxId.?, additionalInfo.?, version.?) <> ((Merchant.fromDbRepr _).tupled, Merchant.toDbRepr)
  }

}

