package io.scalac.controllers

import io.scalac.common.entities.PaginatedResponse
import play.api.libs.json._
import io.scalac.common.play.serializers.Serializers._
import io.scalac.domain.entities._
import io.scalac.domain.services.transport.IncomingNote

import scala.util.Try


// taken from https://github.com/srushingiv/org.skrushingiv/blob/master/src/main/scala/org/skrushingiv/json/EnumerationCombinators.scala
/**
  * An EnumerationReads object attempts to transform a JSON String into an Enumerated value
  * by name. The read will result in a JsError if the value is not a string, or the value cannot
  * be found in the enumeration.
  */
class EnumerationReads[E <: Enumeration](enum: E) extends Reads[E#Value] {
  private def error(s: String, fail: Throwable) = {
    val values = enum.values.map(_.toString).mkString("\"","\", \"","\"")
    "Expected one of [" + values + "] but encountered \"" + s + "\"."
  }

  private def string2value(s: String) = Try(enum.withName(s)).fold(
    { ex =>
      val msg = error(s, ex)
      JsError(msg)
    },
    JsSuccess(_)
  )

  def reads(json:JsValue): JsResult[E#Value] =
    if (json == JsNull) JsSuccess(null) // handle null values without errors
    else Reads.StringReads reads json flatMap string2value // handle non-null values
}

/**
  * An EnumerationWrites object writes enumeration values using their string representation.
  */
trait EnumerationWrites[E <: Enumeration] extends Writes[E#Value] {
  def writes(value:E#Value):JsValue = if (value == null) JsNull else JsString(value.toString)
}

/**
  * An EnumerationFormat object reads and writes enumeration values using their string representation.
  */
class EnumerationFormat[E <: Enumeration](enum: E) extends EnumerationReads(enum) with EnumerationWrites[E]

object Serializers {
  implicit def defaultBillingLangFormat = new EnumerationFormat(DefaultBillingLanguage)
  implicit def paymentTypeFormat        = new EnumerationFormat(PaymentType)
  implicit def commissionTypeFormat     = new EnumerationFormat(CommissionType)
  implicit def netDaysFormat            = new EnumerationFormat(NetDays)

  implicit val merchantInfoFormat = Json.format[MerchantInfo]
  implicit val merchantFormat     = Json.format[Merchant]
  implicit val newNoteFormat      = Json.format[IncomingNote]
  implicit val paginatedResponseMerchantFormat = Json.format[PaginatedResponse[Merchant]]
}
