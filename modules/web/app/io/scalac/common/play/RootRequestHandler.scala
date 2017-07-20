package io.scalac.common.play

import play.api.http._
import play.api.mvc.{Handler, RequestHeader}
import play.api.routing.Router

class RootRequestHandler
(errorHandler: HttpErrorHandler,
 configuration: HttpConfiguration,
 filters: HttpFilters,
 router: Router
) extends DefaultHttpRequestHandler(router, errorHandler, configuration, filters) {

  override def handlerForRequest(request: RequestHeader): (RequestHeader, Handler) = {
    super.handlerForRequest(request)
  }
}
