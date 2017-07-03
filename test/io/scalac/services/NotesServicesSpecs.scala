package io.scalac.services

import java.util.UUID

import cats.syntax.either._
import cats.syntax.option._
import monix.eval.Task
import org.joda.time.DateTime

import io.scalac.common.BaseUnitTest
import io.scalac.common.adapters.NotesDaoAdapter
import io.scalac.common.play.Pagination
import io.scalac.common.services.{DatabaseCallFailed, DatabaseResponse, ServiceFailed}
import io.scalac.domain.entities.Note

class NotesServicesSpecs extends BaseUnitTest {

  "NotesService.find" should {
    "return successful response" when {
      "no note has been found for given id" in new Fixture {
        override def notesDao: NotesDaoAdapter = new NotesDaoAdapter() {
          override def find(noteId: UUID): DatabaseResponse[Option[Note]] =
            Task.now(none.asRight)
        }

        val response = notesService.find(uuid).runAsync.block
        response.isRight shouldBe true
        response shouldBe none.asRight
      }

      "a note has been found for given id" in new Fixture {
        override def notesDao: NotesDaoAdapter = new NotesDaoAdapter() {
          override def find(noteId: UUID): DatabaseResponse[Option[Note]] =
            Task.now(note.some.asRight)
        }

        val response = notesService.find(uuid).runAsync.block
        response.isRight shouldBe true
        response shouldBe outgoingNote.some.asRight
      }
    }

    "return failed response" when {
      "db returns an error" in new Fixture {
        override def notesDao: NotesDaoAdapter = new NotesDaoAdapter() {
          override def find(noteId: UUID): DatabaseResponse[Option[Note]] =
            Task.now(DatabaseCallFailed("BOOM").asLeft)
        }

        val response = notesService.find(uuid).runAsync.block
        response.isLeft shouldBe true
        response shouldBe ServiceFailed("BOOM").asLeft
      }
    }
  }

  "NotesService.list" should {
    "return successful response" when {
      "no notes have been found for given id" in new Fixture {
        override def notesDao: NotesDaoAdapter = new NotesDaoAdapter() {
          override def listAll(pagination: Pagination): DatabaseResponse[Seq[Note]] =
            Task.now(Seq.empty.asRight)
        }

        val response = notesService.list(pagination).runAsync.block
        response.isRight shouldBe true
        response shouldBe Seq.empty.asRight
      }

      "notes have been found for given id" in new Fixture {
        override def notesDao: NotesDaoAdapter = new NotesDaoAdapter() {
          override def listAll(pagination: Pagination): DatabaseResponse[Seq[Note]] =
            Task.now(Seq(note).asRight)
        }

        val response = notesService.list(pagination).runAsync.block
        response.isRight shouldBe true
        response shouldBe Seq(outgoingNote).asRight
      }
    }

    "return failed response" when {
      "db returns an error" in new Fixture {
        override def notesDao: NotesDaoAdapter = new NotesDaoAdapter() {
          override def listAll(pagination: Pagination): DatabaseResponse[Seq[Note]] =
            Task.now(DatabaseCallFailed("BOOM").asLeft)
        }

        val response = notesService.list(pagination).runAsync.block
        response.isLeft shouldBe true
        response shouldBe ServiceFailed("BOOM").asLeft
      }
    }
  }

  trait Fixture {
    val uuid = UUID.randomUUID()
    val note = Note(Some(uuid), "creator", "some not so long note", DateTime.now(), DateTime.now(), Some(1))
    val outgoingNote = OutgoingNote(note.id.get, note.creator, note.note, note.updatedAt)
    val pagination = Pagination(limit = 10, offset = 0)

    def notesDao: NotesDaoAdapter

    val notesService = new DefaultNotesService(notesDao)
  }
}


