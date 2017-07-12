package io.scalac.domain.services.transport

import java.util.UUID

import cats.data.{NonEmptyList, Validated}
import cats.syntax.cartesian._
import org.joda.time.DateTime

import io.scalac.domain.services.ValidatedUtils._

final case class IncomingNote(
  creator: String,
  note: String
) {

  def validate(): Validated[NonEmptyList[String], IncomingNote] = {
    (checkLength(creator, "creator", 3, 100) |@| checkLength(note, "note", 1, 5000))
      .map(IncomingNote.apply)
  }
}

final case class UpdateNote(
  id: UUID,
  incomingNote: IncomingNote
)
