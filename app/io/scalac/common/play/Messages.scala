package io.scalac.common.play

final case class GenericError(error: String)

final case class Pagination(limit: Int, offset: Int) {

  lazy val increase = this.copy(
    offset = offset + limit
  )
}

final case class PaginatedResponse[T](
  next: Pagination,
  sequence: Seq[T]
)
