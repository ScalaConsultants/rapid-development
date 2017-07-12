package io.scalac.common.entities

final case class GenericResponse(message: String)

final case class Pagination(limit: Int, offset: Int) {

  lazy val increase = this.copy(
    offset = offset + limit
  )
}

final case class PaginatedResponse[T](
  next: Pagination,
  sequence: Seq[T],
  counter: Int
)
