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
    val correlation = Correlation.getCorrelation(request.headers.toSimpleMap)
    request.copy(tags = request.tags + (
      Correlation.HeaderCorrelationId -> correlation.correlationId.id,
      Correlation.HeaderUserId -> correlation.userId.fold("")(_.id)))
  }
}
