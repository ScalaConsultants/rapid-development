package io.scalac.domain.entities

import java.util.UUID

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.repository.VersionedRepository
import slick.ast.BaseTypedType
import slick.basic.DatabaseConfig

import io.scalac.common.db.PostgresJdbcProfile

final case class MerchantContactInformation(
  override val id: Option[UUID],
  merchantId: UUID,
  ownerName: String,
  companyName: String,
  phone: Option[String],
  email: Option[String],
  taxId: String,
  notes: Option[String],
  override val version: Option[Int]
) extends VersionedEntity[MerchantContactInformation, UUID, Int] {

  override def withId(id: UUID): MerchantContactInformation = this.copy(id = Some(id))
  override def withVersion(version: Int): MerchantContactInformation = this.copy(version = Some(version))
}

class MerchantContactInformationSlickPostgresRepository (
  val dbConfig: DatabaseConfig[PostgresJdbcProfile]
) extends VersionedRepository[MerchantContactInformation, UUID, Int](dbConfig.profile) {

  import dbConfig.profile.api._
  override type TableType = MerchantContactInformationTable
  override val versionType = implicitly[BaseTypedType[Int]]
  override val pkType = implicitly[BaseTypedType[UUID]]
  override val tableQuery = TableQuery[MerchantContactInformationTable]

  class MerchantContactInformationTable(tag: slick.lifted.Tag) extends Table[MerchantContactInformation](tag, "merchant_contact_information") with Versioned[UUID, Int] {
    override def id = column[UUID]("id", O.PrimaryKey)
    def merchantId = column[UUID]("merchant_id", O.Unique)
    def ownerName = column[String]("owner_name", O.SqlType("VARCHAR(100)"))
    def companyName = column[String]("company_name", O.SqlType("VARCHAR(100)"))
    def phone = column[String]("phone", O.SqlType("VARCHAR(15)"))
    def email = column[String]("email", O.SqlType("VARCHAR(30)"))
    def taxId = column[String]("tax_id", O.SqlType("VARCHAR(30)"))
    def notes = column[String]("notes", O.SqlType("TEXT"))
    override def version = column[Int]("version")

    //    def merchant = foreignKey("merchant_id", merchantId, MerchantsSlickPostgresRepository.tableQuery)(_.id, onUpdate=ForeignKeyAction.Restrict)

    def * = (id.?, merchantId, ownerName, companyName, phone.?, email.?, taxId, notes.?, version.?) <> (
      (MerchantContactInformation.apply _).tupled, MerchantContactInformation.unapply)
  }
}
