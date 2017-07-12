package io.scalac.domain.dao

import java.util.UUID

import io.scalac.common.db.DBExecutor
import io.scalac.common.services.DatabaseResponse
import io.scalac.domain.entities.{Merchant, MerchantSlickPostgresRepository}

trait MerchantsDao {
  def findAll(): DatabaseResponse[Seq[Merchant]]
  // TODO: decide
//  def listAll(pagination: Pagination): DatabaseResponse[Seq[Merchant]]
  def find(merchantId: UUID): DatabaseResponse[Option[Merchant]]
  def create(merchant: Merchant): DatabaseResponse[UUID]
  def update(merchant: Merchant): DatabaseResponse[Merchant]
}

class SlickMerchantsDao (
  merchantsRepo: MerchantSlickPostgresRepository,
  dbExecutor: DBExecutor
) extends MerchantsDao {

  import dbExecutor._
  implicit val ec = dbExecutor.scheduler

  override def findAll(): DatabaseResponse[Seq[Merchant]] =
    merchantsRepo.findAll()

//  override def listAll(pagination: Pagination): DatabaseResponse[Seq[Merchant]] =
//    merchantsRepo.listAll(pagination)

  override def find(merchantId: UUID): DatabaseResponse[Option[Merchant]] =
    merchantsRepo.findOne(merchantId)

  override def create(merchant: Merchant): DatabaseResponse[UUID] =
    merchantsRepo.save(merchant).map(_.id.get)

  override def update(merchant: Merchant): DatabaseResponse[Merchant] =
    merchantsRepo.update(merchant)
}

