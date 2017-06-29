package io.scalac.services

import cats.data.{Validated, ValidatedNel}
import monix.eval.Task

import io.scalac.common.services.{DatabaseResponse, InvalidDbResource, InvalidResource, ServiceResponse}

object ValidatedUtils {

  def checkLength(field: String, name: String, min: Int, max: Int): ValidatedNel[String, String] =
    if (field.length < min || field.length > max)
      Validated.invalid(s"$name's length must be between $min and $max characters").toValidatedNel
    else
      Validated.valid(field)

  implicit class ValidatedOps[B](validatedNel: ValidatedNel[String, B]) {

    import cats.syntax.either._
    def asServiceResponse: ServiceResponse[B] =
      Task.now(validatedNel.toEither.leftMap(errors => InvalidResource(errors.toList)))

    def asDatabaseResponse: DatabaseResponse[B] =
      Task.now(validatedNel.toEither.leftMap(errors => InvalidDbResource(errors.toList)))
  }
}
