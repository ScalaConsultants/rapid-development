package io.scalac.common

import java.util.UUID

package object core {

  final case class UserId(id: String) extends AnyVal

  final case class CorrelationId(id: String) extends AnyVal

  final case class Correlation(correlationId: CorrelationId, userId: Option[UserId]) {

    override def toString: String =
      s"[cid=${correlationId.id}][userId=${userId.map(_.id).getOrElse("")}]"
  }

  object Correlation {

    val NotRequired: String = "NOT_REQUIRED"
    val HeaderCorrelationId: String = "x-correlation-id"
    val HeaderUserId: String = "x-user-id"

    def withNew: Correlation = Correlation(CorrelationId(generateUuid), userId = None)

    def withNotRequired: Correlation = Correlation(CorrelationId(NotRequired), userId = None)

    def apply(correlationId: String): Correlation = Correlation(CorrelationId(correlationId), userId = None)

    def generateUuid: String = UUID.randomUUID().toString

    def getCorrelation(data: Map[String, String]): Correlation = {
      val cid: String = data.getOrElse(HeaderCorrelationId, Correlation.withNew.correlationId.id)
      val userId: Option[String] = data.get(HeaderUserId)
      Correlation(CorrelationId(cid), userId.map(UserId))
    }
  }

}
