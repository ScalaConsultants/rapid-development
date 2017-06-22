package io.scalac.common

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait FuturesSupport {

  implicit class BlockingFuture[A](f: Future[A]) {

    def block = Await.result(f, 5.seconds)
  }
}
