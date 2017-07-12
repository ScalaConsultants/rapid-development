package io.scalac.common

import io.scalac.common.core.Correlation

package object auth {

  //authenticated user
  sealed trait Subject

  //user on behalf subject is working, a subject is ordinary user or admin
  sealed trait Delegate

  final case class ServiceContext(
    correlation: Correlation,
    properties: Map[String, String], //TODO Consider changing to expendable case class
    subject: Option[Subject],
    delegate: Option[Delegate]
  )

  def EmptyContext(c: Correlation = Correlation.withNew) = ServiceContext(
    correlation = c,
    properties = Map.empty,
    subject = None,
    delegate = None
  )
}
