package io.scalac.services

import com.google.inject.{Inject, Singleton}

import io.scalac.common.services.Response
import io.scalac.domain.dao.NotesDao
import io.scalac.domain.entities.Note

trait NotesService {

  def findAll(): Response[Seq[Note]]
}

@Singleton
class DefaultNotesService @Inject() (
  notesDao: NotesDao
) extends NotesService {

  import io.scalac.common.services._

  override def findAll(): Response[Seq[Note]] = {
    //FIXME quite annoying to do it all the times...
    // get rid or introduce common code to map over all possible DB errors ~toServiceResponse...
    //TODO what to do with db error? Map to Service error or just pass through?
    notesDao.findAll().toServiceResponse
  }
}
