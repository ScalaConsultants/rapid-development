package io.scalac.common.services

import java.util.UUID

//TODO use everywhere UUID is used directly

trait IdGenerator[ID] {
  def get: ID
}

class UuidIdGenerator extends IdGenerator[UUID] {

  override def get: UUID = UUID.randomUUID()
}
