package io.scalac.common.play

import com.google.inject.{Inject, Singleton}
import play.api.http._
import play.api.mvc.{Handler, RequestHeader}
import play.api.routing.Router

import io.scalac.common.core.Correlation

@Singleton
class RootRequestHandler @Inject()
(errorHandler: HttpErrorHandler,
 configuration: HttpConfiguration,
 filters: HttpFilters,
 router: Router
) extends DefaultHttpRequestHandler(router, errorHandler, configuration, filters) {

  override def handlerForRequest(request: RequestHeader): (RequestHeader, Handler) = {
    super.handlerForRequest(addCorrelation(request))
  }

  private def addCorrelation(request: RequestHeader) = {
    //TODO with Play 2.6 use Attr
    val cid: String = request.headers.get(Correlation.HeaderCorrelationId).getOrElse(Correlation.withNew.toString)
    val userId: Option[String] = request.headers.get(Correlation.HeaderUserId)
    request.copy(tags = request.tags + (
      Correlation.HeaderCorrelationId -> cid,
      Correlation.HeaderUserId -> userId.getOrElse("")))
  }
}
