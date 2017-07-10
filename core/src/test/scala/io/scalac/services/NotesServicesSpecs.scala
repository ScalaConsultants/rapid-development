package io.scalac.services

import java.util.UUID

import cats.syntax.either._
import cats.syntax.option._
import io.scalac.common.entities.Pagination
import io.scalac.common.services.{DatabaseCallFailed, DatabaseResponse, InvalidResource, MissingResource, ServiceFailed}
import io.scalac.domain.BaseUnitTest
import io.scalac.domain.mock.NotesDaoMock
import io.scalac.domain.entities.Note
import io.scalac.domain.services.DefaultNotesService
import io.scalac.domain.services.transport.{IncomingNote, OutgoingNote, UpdateNote}
import monix.eval.Task
import org.joda.time.DateTime

class NotesServicesSpecs extends BaseUnitTest {

  "NotesService.find" should {
    "return successful response" when {
      "no note has been found for given id" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock() {
          override def find(noteId: UUID): DatabaseResponse[Option[Note]] =
            Task.now(none.asRight)
        }

        val response = notesService.find(uuid).runAsync.block
        response shouldBe none.asRight
      }

      "a note has been found for given id" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock() {
          override def find(noteId: UUID): DatabaseResponse[Option[Note]] =
            Task.now(dbNote.some.asRight)
        }

        val response = notesService.find(uuid).runAsync.block
        response shouldBe outgoingNote.some.asRight
      }
    }

    "return failed response" when {
      "db returns an error" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock() {
          override def find(noteId: UUID): DatabaseResponse[Option[Note]] =
            Task.now(DatabaseCallFailed("BOOM").asLeft)
        }

        val response = notesService.find(uuid).runAsync.block
        response shouldBe ServiceFailed("BOOM").asLeft
      }
    }
  }

  "NotesService.list" should {
    "return successful response" when {
      "no notes have been found for given id" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock() {
          override def listAll(pagination: Pagination): DatabaseResponse[Seq[Note]] =
            Task.now(Seq.empty.asRight)
        }

        val response = notesService.list(pagination).runAsync.block
        response shouldBe Seq.empty.asRight
      }

      "notes have been found for given id" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock() {
          override def listAll(pagination: Pagination): DatabaseResponse[Seq[Note]] =
            Task.now(Seq(dbNote).asRight)
        }

        val response = notesService.list(pagination).runAsync.block
        response shouldBe Seq(outgoingNote).asRight
      }
    }

    "return failed response" when {
      "db returns an error" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock() {
          override def listAll(pagination: Pagination): DatabaseResponse[Seq[Note]] =
            Task.now(DatabaseCallFailed("BOOM").asLeft)
        }

        val response = notesService.list(pagination).runAsync.block
        response shouldBe ServiceFailed("BOOM").asLeft
      }
    }
  }

  "NotesService.create" should {
    "create note successfully" when {
      "valid note is provided" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock() {
          override def create(note: Note): DatabaseResponse[UUID] =
            Task.now(uuid.asRight)
        }

        val newNote = IncomingNote(dbNote.creator, dbNote.note)

        val response = notesService.create(newNote).runAsync.block
        response shouldBe uuid.asRight
      }
    }

    "return failed response and not create a note" when {

      "provided note has too short creator field" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock()

        val newNote = IncomingNote("A", dbNote.note)

        val response = notesService.create(newNote).runAsync.block
        response shouldBe InvalidResource(Seq("creator's length must be between 3 and 100 characters")).asLeft
      }

      "provided note has too short note field" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock()

        val newNote = IncomingNote(dbNote.creator, "")

        val response = notesService.create(newNote).runAsync.block
        response shouldBe InvalidResource(Seq("note's length must be between 1 and 5000 characters")).asLeft
      }

      "db returns an error" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock() {
          override def create(note: Note): DatabaseResponse[UUID] =
            Task.now(DatabaseCallFailed("BOOM").asLeft)
        }
        val newNote = IncomingNote(dbNote.creator, dbNote.note)

        val response = notesService.create(newNote).runAsync.block
        response shouldBe ServiceFailed("BOOM").asLeft
      }
    }
  }

  "NotesService.update" should {
    "update note successfully" when {
      "existing note of given ID is found and valid note is provided" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock() {
          override def find(noteId: UUID): DatabaseResponse[Option[Note]] =
            Task.now(dbNote.some.asRight)

          override def update(note: Note): DatabaseResponse[Note] =
            Task.now(note.copy(version = dbNote.version.map(_ + 1)).asRight)
        }

        val noteToUpdate = IncomingNote(dbNote.creator + "Superman", dbNote.note)
        val updateNote = UpdateNote(uuid, noteToUpdate)

        val response = notesService.update(updateNote).runAsync.block
        val updatedNote = response.right.get
        assert(updatedNote.recentEdit.isAfter(dbNote.updatedAt), "Recent update timestamp must be later than the one already persisted")
        updatedNote.creator shouldBe noteToUpdate.creator
        updatedNote.note shouldBe noteToUpdate.note
        updatedNote.id shouldBe updateNote.id
      }
    }

    "return failed response and not update a note" when {

      "provided note has too short creator field" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock()

        val noteToUpdate = IncomingNote("BC", dbNote.note)
        val updateNote = UpdateNote(uuid, noteToUpdate)

        val response = notesService.update(updateNote).runAsync.block
        response shouldBe InvalidResource(Seq("creator's length must be between 3 and 100 characters")).asLeft
      }

      "provided note has empty short note field" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock()

        val noteToUpdate = IncomingNote(dbNote.creator, "")
        val updateNote = UpdateNote(uuid, noteToUpdate)

        val response = notesService.update(updateNote).runAsync.block
        response shouldBe InvalidResource(Seq("note's length must be between 1 and 5000 characters")).asLeft
      }

      "existing note of given ID is not found" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock() {
          override def find(noteId: UUID): DatabaseResponse[Option[Note]] =
            Task.now(none.asRight)
        }

        val noteToUpdate = IncomingNote(dbNote.creator + "Superman", dbNote.note)
        val updateNote = UpdateNote(uuid, noteToUpdate)

        val response = notesService.update(updateNote).runAsync.block
        response shouldBe MissingResource("Cannot update non-existent element").asLeft
      }

      "db returns an error" in new Fixture {
        override def notesDao: NotesDaoMock = new NotesDaoMock() {
          override def create(note: Note): DatabaseResponse[UUID] =
            Task.now(DatabaseCallFailed("BOOM").asLeft)
        }
        val newNote = IncomingNote(dbNote.creator, dbNote.note)

        val response = notesService.create(newNote).runAsync.block
        response shouldBe ServiceFailed("BOOM").asLeft
      }
    }
  }

  trait Fixture {
    val uuid = UUID.randomUUID()
    val dbNote = Note(Some(uuid), "creator", "some not so long note", DateTime.now(), DateTime.now(), Some(1))
    val outgoingNote = OutgoingNote(dbNote.id.get, dbNote.creator, dbNote.note, dbNote.updatedAt)
    val pagination = Pagination(limit = 10, offset = 0)

    def notesDao: NotesDaoMock

    val notesService = new DefaultNotesService(notesDao)
  }
}
