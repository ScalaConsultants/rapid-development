package io.scalac.common.play.serializers

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

object Serializers {

  val dateTimeFormatPattern = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  val dateTimeReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, dateTimeFormatPattern)
    )
  )
  val dateTimeWrites: Writes[DateTime] = new Writes[DateTime] {
    def writes(d: DateTime): JsValue = JsString(d.toString(dateTimeFormatPattern))
  }
  implicit val dateTimeFormat = Format(dateTimeReads, dateTimeWrites)

}
