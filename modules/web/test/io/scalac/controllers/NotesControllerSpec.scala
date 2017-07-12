package io.scalac.controllers

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.scalac.common.core.Correlation
import io.scalac.common.entities.Pagination
import io.scalac.common.play.RequestAttributes
import io.scalac.common.services.{InvalidResource, MissingResource, NoopServiceProfiler, Service, ServiceError, ServiceFailed}
import io.scalac.controllers.mock.NotesServiceMock
import io.scalac.domain.BaseUnitTest
import io.scalac.domain.services.NotesService
import io.scalac.domain.services.transport.{IncomingNote, OutgoingNote, UpdateNote}
import monix.eval.Task
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfterAll
import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}

class NotesControllerSpec extends BaseUnitTest with BeforeAndAfterAll {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  override def afterAll(): Unit = {
    mat.shutdown()
    system.terminate()
  }

  "NotesController.all" should {
    val request =
      FakeRequest(GET, s"/notes")
        .addAttr(RequestAttributes.Correlation, Correlation("cid"))

    "work" in new Fixture {
      val itemsNumber = 4

      val service = new NotesServiceMock {
        override def list: Service[Pagination, Seq[OutgoingNote], ServiceError] = mockService { _ =>
          Task.now(Right(List.fill(itemsNumber)(outgoingNote)))
        }
      }

      val res = controller.all(10, 0).apply(request)

      status(res) should equal (OK)
      val responseJson = contentAsJson(res).asJsObject
      responseJson("next").asJsObject.fields.map(_._1).toSet should equal (Set("limit", "offset"))
      responseJson("sequence").asJsArray.value.size should equal(itemsNumber)
    }
  }

  "NotesController.find" should {
    def request(id: UUID) =
      FakeRequest(GET, s"/notes/${id.toString}")
        .addAttr(RequestAttributes.Correlation, Correlation("cid"))

    "return OK in case note has been found" in new Fixture {
      val service = new NotesServiceMock {
        override def find: Service[UUID, Option[OutgoingNote], ServiceError] = mockService { _ =>
          Task.now(Right(Some(outgoingNote)))
        }
      }

      val res = controller.find(uuid).apply(request(uuid))

      status(res) should equal (OK)
      val responseJson = contentAsJson(res).asJsObject
      responseJson.fields.map(_._1).toSet should equal (Set("id", "creator", "note", "recentEdit"))
    }

    "return NOT_FOUND in case note has been found" in new Fixture {
      val service = new NotesServiceMock {
        override def find: Service[UUID, Option[OutgoingNote], ServiceError] = mockService { _ =>
          Task.now(Right(None))
        }
      }

      val res = controller.find(uuid).apply(request(uuid))

      status(res) should equal (NOT_FOUND)
      val responseJson = contentAsJson(res).asJsObject
      responseJson.fields.map(_._1).toSet should equal (Set("message"))
    }

  }

  "NotesController.update" should {
    def request(id: UUID, jsonString: String) =
      FakeRequest(PUT, s"/notes/${id.toString}")
        .withJsonBody(Json.parse(jsonString))
        .addAttr(RequestAttributes.Correlation, Correlation("cid"))

    val correctJsonString =
      """
        |{
        |  "creator" : "Different author",
        |  "note": "lorem ipsum"
        |}
      """.stripMargin

    "return OK if everything went fine" in new Fixture {
      val service = new NotesServiceMock {
        override def update: Service[UpdateNote, OutgoingNote, ServiceError] = mockService { _ =>
          Task.now(Right(outgoingNote))
        }
      }

      val req = request(uuid, correctJsonString)
      val res = controller.update(uuid).apply(req)

      status(res) should equal (OK)
      val responseJson = contentAsJson(res).asJsObject
      responseJson.fields.map(_._1).toSet should equal (Set("id", "creator", "note", "recentEdit"))
    }

    "return NOT_FOUND if requested resource not found" in new Fixture {
      val service = new NotesServiceMock {
        override def update: Service[UpdateNote, OutgoingNote, ServiceError] = mockService { _ =>
          Task.now(Left(MissingResource("missing")))
        }
      }

      val req = request(uuid, correctJsonString)
      val res = controller.update(uuid).apply(req)

      status(res) should equal (NOT_FOUND)
    }

    "return INTERNAL_SERVER_ERROR if NotesService failed" in new Fixture {
      val service = new NotesServiceMock {
        override def update: Service[UpdateNote, OutgoingNote, ServiceError] = mockService { _ =>
          Task.now(Left(ServiceFailed("some err")))
        }
      }

      val req = request(uuid, correctJsonString)
      val res = controller.update(uuid).apply(req)

      status(res) should equal (INTERNAL_SERVER_ERROR)
    }

    "return BAD_REQUEST if object being updated is not valid" in new Fixture {
      val service = new NotesServiceMock {
        override def update: Service[UpdateNote, OutgoingNote, ServiceError] = mockService { _ =>
          Task.now(Left(InvalidResource(List("validation error"))))
        }
      }

      val req = request(uuid, correctJsonString)
      val res = controller.update(uuid).apply(req)

      status(res) should equal (BAD_REQUEST)
    }

    "return BAD_REQUEST if request JSON cannot be deserialized to business object" in new Fixture {
      val service = new NotesServiceMock

      val req = request(uuid, "{}")
      val res = controller.update(uuid).apply(req)

      status(res) should equal (BAD_REQUEST)
    }

  }

  "NotesController.create" should {
    def request(jsonString: String) =
      FakeRequest(POST, "/notes")
        .withJsonBody(Json.parse(jsonString))
        .addAttr(RequestAttributes.Correlation, Correlation("cid"))

    val correctJsonString =
      """{
        |	"creator": "Bob",
        |	"note": "It is some note"
        |}
      """.stripMargin

    "return CREATED if everything went fine" in new Fixture {
      val service = new NotesServiceMock {
        override def create: Service[IncomingNote, UUID, ServiceError] = mockService { _ =>
          Task.now(Right(uuid))
        }
      }

      val req = request(correctJsonString)

      val res = controller.create().apply(req)
      status(res) should equal(CREATED)
      (contentAsJson(res) \ "message").get should equal (JsString(uuid.toString))
    }

    "return BAD_REQUEST if incomplete request's JSON" in new Fixture {
      val service = new NotesServiceMock {
        override def create: Service[IncomingNote, UUID, ServiceError] = mockService { _ =>
          Task.now(Right(uuid))
        }
      }

      val jsonString = "{}"
      val req = request(jsonString)

      val res = controller.create().apply(req)
      status(res) should equal(BAD_REQUEST)
    }

    "return INTERNAL_SERVER_ERROR if NotesService failed" in new Fixture {
      val service = new NotesServiceMock {
        override def create: Service[IncomingNote, UUID, ServiceError] = mockService { _ =>
          Task.now(Left(ServiceFailed("some err")))
        }
      }

      val req = request(correctJsonString)

      val res = controller.create().apply(req)
      status(res) should equal(INTERNAL_SERVER_ERROR)
    }
  }

  trait Fixture {
    def service: NotesService

    val uuid = UUID.randomUUID
    val someTime = new DateTime

    val scheduler = monix.execution.Scheduler.Implicits.global

    val outgoingNote = OutgoingNote(uuid, "creator", "note", someTime)

    // has to be lazy as `service` is not initialized when TestContext constructor is being initialized
    lazy val controller = {
      implicit val profiler = NoopServiceProfiler
      implicit val controllerComponents = stubControllerComponents()

      new NotesController(service, scheduler)
    }
  }

}
