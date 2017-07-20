package io.scalac.domain

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

trait FuturesSupport extends ScalaFutures {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  implicit class BlockingFuture[A](f: Future[A]) {

    def block = Await.result(f, 5.seconds)
  }
}
