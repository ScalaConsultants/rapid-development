package io.scalac.common

import java.util.UUID

import org.joda.time.DateTime

package object core {

  final case class UserId(id: UUID) extends AnyVal
  final case class TokenId(id: UUID) extends AnyVal
  final case class AuthenticationProviderId(id: UUID) extends AnyVal
  final case class AuthToken(
    token: TokenId,
    userId: UserId,
    expiry: DateTime
  )
  final case class AuthorizationToken(id: String) extends AnyVal

  final case class CorrelationId(id: UUID) extends AnyVal

  final case class Correlation(correlationId: CorrelationId, userId: Option[UserId]) {

    override def toString: String =
      s"[cid=${correlationId.id}][userId=${userId.map(_.id).getOrElse("")}]"
  }

  object Correlation {

    val NotRequired: UUID = UUID.fromString("00000000-0000-0000-0000-00000000000")
    val HeaderCorrelationId: String = "X-Correlation-Id"
    val HeaderUserId: String = "X-User-Id"

    def withNew: Correlation = Correlation(CorrelationId(generateUuid), userId = None)

    def withNotRequired: Correlation = Correlation(CorrelationId(NotRequired), userId = None)

    def apply(correlationId: String): Correlation = Correlation(CorrelationId(UUID.fromString(correlationId)), userId = None)

    def generateUuid: UUID = UUID.randomUUID()

    def getCorrelation(data: Map[String, String]): Correlation = {
      val cid = data.getOrElse(HeaderCorrelationId, Correlation.withNew.correlationId.id.toString)
      val userId: Option[String] = data.get(HeaderUserId)
      Correlation(CorrelationId(UUID.fromString(cid)), userId.map(id => UserId(UUID.fromString(id))))
    }
  }

}
