package io.scalac.domain.dao

import com.google.inject.{Inject, Singleton}

import io.scalac.common.play.Pagination
import io.scalac.common.services.DBResponse
import io.scalac.domain.entities.{Note, NotesSlickPostgresRepository}

trait NotesDao {

//  def findAll: Service[Unit, Seq[Note], DatabaseError] //TODO decide
  def findAll(): DBResponse[Seq[Note]]
  def listAll(pagination: Pagination): DBResponse[Seq[Note]]
}

@Singleton
class SlickNotesDao @Inject() (
  notesRepo: NotesSlickPostgresRepository,
  dBImplicits: DBImplicits
) extends NotesDao {

  import dBImplicits._

  override def findAll(): DBResponse[Seq[Note]] = {
    notesRepo.findAll()
  }

  override def listAll(pagination: Pagination): DBResponse[Seq[Note]] = {
    notesRepo.listAll(pagination)
  }
}
