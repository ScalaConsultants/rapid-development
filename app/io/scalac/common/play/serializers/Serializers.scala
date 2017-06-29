package io.scalac.common.play.serializers

import scala.language.implicitConversions

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import io.scalac.common.play.{GenericResponse, Pagination}

object Serializers {

  val dateTimeFormatPattern = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  private val dateTimeReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, dateTimeFormatPattern)
    )
  )
  private val dateTimeWrites: Writes[DateTime] = new Writes[DateTime] {
    def writes(d: DateTime): JsValue = JsString(d.toString(dateTimeFormatPattern))
  }
  implicit val dateTimeFormat = Format(dateTimeReads, dateTimeWrites)

  implicit val genericErrorFormat = Json.format[GenericResponse]
  implicit val paginationFormat = Json.format[Pagination]

  implicit class JsonImplicits[T: OWrites](obj: T) {

    def asJson = Json.prettyPrint(Json.toJson(obj))
  }

  implicit class JsonTraversableImplicits[T: OWrites](obj: Traversable[T]) {

    def asJson = Json.prettyPrint(Json.toJson(obj))
  }
}
