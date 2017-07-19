package io.scalac.common.play.serializers

import java.util.UUID

import scala.language.implicitConversions
import scala.util.Try

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import io.scalac.common.core.{TokenId, UserId}
import io.scalac.common.entities.{GenericResponse, Pagination}
import io.scalac.controllers.auth.{IncomingSignIn, IncomingSignUp}

object Serializers {

  val dateTimeFormatPattern = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  private val dateTimeReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, dateTimeFormatPattern)
    )
  )
  private val dateTimeWrites: Writes[DateTime] = (d: DateTime) => JsString(d.toString(dateTimeFormatPattern))
  implicit val dateTimeFormat = Format(dateTimeReads, dateTimeWrites)

  implicit val genericErrorFormat = Json.format[GenericResponse]
  implicit val paginationFormat = Json.format[Pagination]

  implicit val tokenIdFormat = new Format[TokenId] {
    override def reads(json: JsValue): JsResult[TokenId] =
      Try(JsSuccess(TokenId(UUID.fromString(json.as[String]))))
        .getOrElse(JsError(s"${json.toString()}] is not valid UUID type"))

    override def writes(o: TokenId): JsValue = JsString(o.id.toString)
  }

  implicit val userIdFormat = new Format[UserId] {
    override def reads(json: JsValue): JsResult[UserId] =
      Try(JsSuccess(UserId(UUID.fromString(json.as[String]))))
        .getOrElse(JsError(s"${json.toString()}] is not valid UUID type"))

    override def writes(o: UserId): JsValue = JsString(o.id.toString)
  }

  implicit val incomingSignUpFormat = Json.format[IncomingSignUp]
  implicit val incomingSignInFormat = Json.format[IncomingSignIn]

  implicit class JsonImplicits[T: OWrites](obj: T) {

    def asJson = Json.prettyPrint(Json.toJson(obj))
  }

  implicit class JsonTraversableImplicits[T: OWrites](obj: Traversable[T]) {

    def asJson = Json.prettyPrint(Json.toJson(obj))
  }
}
