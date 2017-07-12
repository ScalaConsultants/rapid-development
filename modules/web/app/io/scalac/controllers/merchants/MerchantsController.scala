package io.scalac.controllers.merchants

import monix.execution.Scheduler
import org.joda.time.DateTime
import play.api.mvc.{AbstractController, ControllerComponents}

import io.scalac.common.entities.Pagination
import io.scalac.common.logger.Logging
import io.scalac.common.play.ControllerHelper
import io.scalac.common.services.ServiceProfiler
import io.scalac.controllers.Serializers
import io.scalac.domain.entities.{CommissionTypes, CycleTypes, PaymentTypes}
import io.scalac.domain.services.merchants.{ListMerchantSearchCriteria, MerchantsService}

class MerchantsController (
  merchantsService: MerchantsService,
  scheduler: Scheduler)
  (implicit profiler: ServiceProfiler, controllerComponents: ControllerComponents)
  extends AbstractController(controllerComponents) with Logging with ControllerHelper {

  import Serializers._
  import io.scalac.common.play.serializers.Serializers._

  private implicit val schedulerImpl = scheduler

  def search(active: Boolean,
    billingCycle: String,
    paymentType: String,
    commissionType: String,
    lastInvoiceDate: Option[String],
    limit: Int,
    offset: Int) = noEntity { implicit request => implicit ctx => implicit corr =>
    val pagination = Pagination(limit = limit, offset = offset)

    val criteria = ListMerchantSearchCriteria(
      active,
      CycleTypes.withName(billingCycle),
      PaymentTypes.withName(paymentType),
      CommissionTypes.withName(commissionType),
      lastInvoiceDate.map(DateTime.parse(_, dateTimeFormatPattern)),
      pagination)

    merchantsService.list(criteria).runAsync.map {
      _.fold(
        otherErrorsHandler,
        paginatedResponse => {
          logger.info(s"${request.path} - successful response")
          Ok(paginatedResponse.asJson)
        }
      )
    }
  }
}
