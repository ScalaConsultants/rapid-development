package io.scalac.services

import com.google.inject.{Inject, Singleton}

import io.scalac.common.play.Pagination
import io.scalac.common.services._
import io.scalac.domain.dao.NotesDao
import io.scalac.domain.entities.Note


trait NotesService {

  def findAll(): Response[Seq[Note]] //TODO prepare Service without request...
  def list: Service[Pagination, Seq[Note], ServiceError]
}

@Singleton
class DefaultNotesService @Inject() (
  notesDao: NotesDao
) extends NotesService {

  override def findAll(): Response[Seq[Note]] = {
    //TODO what to do with db error? Map to Service error or just pass through?
    notesDao.findAll().toServiceResponse
  }

  override def list: Service[Pagination, Seq[Note], ServiceError] =
    Service("io.scalac.services.DefaultNotesService.list") { req => implicit serviceContext =>
    notesDao.listAll(req).toServiceResponse
  }
}