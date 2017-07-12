package io.scalac.domain.services.merchants

import io.scalac.common.entities.PaginatedResponse
import io.scalac.common.logger.Logging
import io.scalac.common.services.{Service, ServiceError, ServiceProfiler, _}
import io.scalac.common.syntax._
import io.scalac.domain.dao.MerchantsDao

trait MerchantsService {

  def list: Service[ListMerchantSearchCriteria, PaginatedResponse[OutgoingMerchant], ServiceError]
}

class DefaultMerchantsService(
  merchantsDao: MerchantsDao
)(implicit val profiler: ServiceProfiler) extends MerchantsService with Logging {

  override def list: Service[ListMerchantSearchCriteria, PaginatedResponse[OutgoingMerchant], ServiceError] =
    Service("io.scalac.services.DefaultMerchantsService.list") { req => _ =>
      merchantsDao.find(req).tmap { paginatedMerchants =>
        val outgoing = paginatedMerchants.merchants.map { case (merchant, merchantInvoice) =>
          Conversions.toOutgoingMerchant(merchant, merchantInvoice)
        }

        PaginatedResponse(paginatedMerchants.next, outgoing, paginatedMerchants.count)
      }.toServiceResponse
    }

}
