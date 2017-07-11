package io.scalac.common.play.serializers

import io.scalac.common.entities.{GenericResponse, Pagination}
import scala.language.implicitConversions

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import io.scalac.controllers.auth.{AuthToken, IncomingSignUp}

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

  implicit val incomingSignUpFormat = Json.format[IncomingSignUp]
  implicit val authTokenFormat = Json.format[AuthToken]

  implicit class JsonImplicits[T: OWrites](obj: T) {

    def asJson = Json.prettyPrint(Json.toJson(obj))
  }

  implicit class JsonTraversableImplicits[T: OWrites](obj: Traversable[T]) {

    def asJson = Json.prettyPrint(Json.toJson(obj))
  }
}
