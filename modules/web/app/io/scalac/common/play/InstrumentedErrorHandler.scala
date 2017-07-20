package io.scalac.common.play

import scala.concurrent.{ExecutionContext, Future}

import play.api.http.HttpErrorHandler
import play.api.mvc.{RequestHeader, Result}

trait InstrumentedErrorHandler extends HttpErrorHandler {

  implicit val ec: ExecutionContext

  //TODO Inject ServiceProfiler or any other and measure time execution as well result of entire request

  abstract override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    //Maps over result to have it invoked last in the chain
    super.onClientError(request, statusCode, message)
  }

  abstract override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    //Maps over result to have it invoked last in the chain
    super.onServerError(request, exception)
  }
}
