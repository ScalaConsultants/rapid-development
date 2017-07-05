package io.scalac.domain

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

// TODO: Probably should be removed in favor for scalatest's ScalaFutures
trait FuturesSupport {

  implicit class BlockingFuture[A](f: Future[A]) {

    def block = Await.result(f, 5.seconds)
  }
}
