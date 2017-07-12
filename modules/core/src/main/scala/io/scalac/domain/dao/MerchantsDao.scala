package io.scalac.domain.dao

import java.util.UUID

import io.scalac.common.db.DBExecutor
import io.scalac.common.entities.Pagination
import io.scalac.common.services.DatabaseResponse
import io.scalac.domain.entities._
import io.scalac.domain.services.merchants.ListMerchantSearchCriteria

final case class PaginatedMerchants(count: Int, merchants: Seq[(Merchant, MerchantInvoices)], next: Pagination)

trait MerchantsDao {

  def find(criteria: ListMerchantSearchCriteria): DatabaseResponse[PaginatedMerchants]
  def listAll(pagination: Pagination): DatabaseResponse[Seq[Merchant]]
  def findOne(merchantId: UUID): DatabaseResponse[Option[Merchant]]
  def create(merchant: Merchant): DatabaseResponse[UUID]
  def update(merchant: Merchant): DatabaseResponse[Merchant]
}

class SlickMerchantsDao (
  merchantsRepo: MerchantsSlickPostgresRepository,
  merchantInvoicesRepo: MerchantInvoicesSlickPostgresRepository,
  billingSettingsRepo: MerchantBillingSettingsSlickPostgresRepository,
  dbExecutor: DBExecutor
) extends MerchantsDao with MerchantCommonMappers {

  import dbExecutor._
  implicit val ec = dbExecutor.scheduler

  override def find(criteria: ListMerchantSearchCriteria): DatabaseResponse[PaginatedMerchants] = {
//  TODO hmmm, don't like it. Need to define queries and join tables on a repository level probably
    import merchantsRepo.dbConfig.profile.api._

    val q = for {
      ((m, b), i) <- (merchantsRepo.tableQuery join billingSettingsRepo.tableQuery on (_.id === _.merchantId))
                        .join(merchantInvoicesRepo.tableQuery).on(_._1.id === _.merchantId)
        if m.active === criteria.active  && b.cycleType === criteria.billingCycle && b.commissionType === criteria.commissionType &&
          b.paymentType === criteria.paymentType // && i.lastInvoiceDate === criteria.lastInvoiceDate
    } yield (m, i)

    for {
      data <- Compiled(q.drop(criteria.pagination.offset).take(criteria.pagination.limit)).result
      counter <- Compiled(q.length).result //for given set of criterion, counter could be calculated once
    } yield {
      PaginatedMerchants(counter, data, criteria.pagination.increase)
    }
  }

  override def listAll(pagination: Pagination): DatabaseResponse[Seq[Merchant]] =
    merchantsRepo.listAll(pagination)

  override def findOne(merchantId: UUID): DatabaseResponse[Option[Merchant]] =
    merchantsRepo.findOne(merchantId)

  override def create(merchant: Merchant): DatabaseResponse[UUID] =
    merchantsRepo.save(merchant).map(_.id.get)

  override def update(merchant: Merchant): DatabaseResponse[Merchant] =
    merchantsRepo.update(merchant)
}

