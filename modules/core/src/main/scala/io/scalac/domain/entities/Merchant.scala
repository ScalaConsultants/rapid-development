package io.scalac.domain.entities

import java.util.UUID

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.repository.VersionedRepository
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.basic.DatabaseConfig

import io.scalac.common.db.PostgresJdbcProfile
import io.scalac.common.entities.Pagination

final case class Merchant(
  override val id: Option[UUID],
  merchantNo: Int,
  name: String,
  city: String,
  country: String,
  active: Boolean,
  createdAt: DateTime,
  updatedAt: DateTime,
  override val version: Option[Int]
) extends VersionedEntity[Merchant, UUID, Int] {

  override def withId(id: UUID): Merchant = this.copy(id = Some(id))
  override def withVersion(version: Int): Merchant = this.copy(version = Some(version))
}

class MerchantsSlickPostgresRepository (
  val dbConfig: DatabaseConfig[PostgresJdbcProfile]
) extends VersionedRepository[Merchant, UUID, Int](dbConfig.profile) {

  import dbConfig.profile.api._
  override type TableType = MerchantsTable
  override val versionType = implicitly[BaseTypedType[Int]]
  override val pkType = implicitly[BaseTypedType[UUID]]
  override val tableQuery = TableQuery[MerchantsTable]

  class MerchantsTable(tag: slick.lifted.Tag) extends Table[Merchant](tag, "merchants") with Versioned[UUID, Int] {
    override def id = column[UUID]("id", O.PrimaryKey)
    def merchantNo = column[Int]("merchant_no", O.Unique)
    def name = column[String]("name", O.SqlType("VARCHAR(200)"))
    def city = column[String]("city", O.SqlType("VARCHAR(100)"))
    def country = column[String]("country", O.SqlType("VARCHAR(100)"))
    def active = column[Boolean]("active")
    def createdAt = column[DateTime]("created_at", O.SqlType("timestampz"))
    def updatedAt = column[DateTime]("updated_at", O.SqlType("timestampz"))
    override def version = column[Int]("version")

    def * = (id.?, merchantNo, name, city, country, active, createdAt, updatedAt, version.?) <> ((
      Merchant.apply _).tupled, Merchant.unapply)
  }

  //TODO it should return pages/allCount too
  def listAll(pagination: Pagination): DBIO[Seq[Merchant]] = {
    Compiled(tableQuery.drop(pagination.offset).take(pagination.limit)).result
  }

  def find(pagination: Pagination): DBIO[Seq[Merchant]] = {
    Compiled(tableQuery.drop(pagination.offset).take(pagination.limit)).result
  }
}
