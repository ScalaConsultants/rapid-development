package io.scalac.services

import cats.data.{Validated, ValidatedNel}

object ValidatedUtils {

  def checkLength(field: String, name: String, min: Int, max: Int): ValidatedNel[String, String] =
    if (field.length < min || field.length > max)
      Validated.invalid(s"$name's length must be between $min and $max characters").toValidatedNel
    else
      Validated.valid(field)
}
