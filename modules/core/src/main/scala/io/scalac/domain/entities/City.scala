package io.scalac.domain.entities

import java.util.UUID

import com.byteslounge.slickrepo.meta.VersionedEntity

case class City (
  override val id: Option[UUID],
  name: String,
  country: String,
  override val version: Option[Int]
) extends VersionedEntity[City, UUID, Int] {

  override def withVersion(version: Int): City  = copy(version = Some(version))
  override def withId(id: UUID): City           = copy(id = Some(id))
}
