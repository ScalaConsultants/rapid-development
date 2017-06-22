package io.scalac.common.logger

trait Logging {

  implicit val logger = AppLogger(getClass)
}
