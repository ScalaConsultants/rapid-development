package io.scalac.controllers

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.scalac.common.adapters.NotesServiceProto
import io.scalac.common.core.Correlation
import io.scalac.common.play.RequestAttributes
import io.scalac.common.services.{Context, Logger, NoopServiceProfiler, Profiler, Service, ServiceError, ServiceFailed}
import io.scalac.services._
import monix.eval.Task
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import play.api.libs.json._
import play.api.test.Helpers.{status, _}
import play.api.test.{FakeRequest, StubControllerComponentsFactory}

class NotesControllerSpec extends WordSpec with Matchers with StubControllerComponentsFactory with ScalaFutures with BeforeAndAfterAll {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  override def afterAll(): Unit = {
    mat.shutdown()
    system.terminate()
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

    "return CREATED if everything went fine" in new TestContext {
      val service = new NotesServiceProto {
        override def create: Service[IncomingNote, UUID, ServiceError] = mockService { _ =>
          Task.now(Right(uuid))
        }
      }

      val req = request(correctJsonString)

      val res = controller.create().apply(req)
      status(res) should equal(CREATED)
      (contentAsJson(res) \ "message").get should equal (JsString(uuid.toString))
    }

    "return BAD_REQUEST if incomplete request's JSON" in new TestContext {
      val service = new NotesServiceProto {
        override def create: Service[IncomingNote, UUID, ServiceError] = mockService { _ =>
          Task.now(Right(uuid))
        }
      }

      val jsonString = "{}"
      val req = request(jsonString)

      val res = controller.create().apply(req)
      status(res) should equal(BAD_REQUEST)
    }

    "return INTERNAL_SERVER_ERROR if NotesService failed" in new TestContext {
      val service = new NotesServiceProto {
        override def create: Service[IncomingNote, UUID, ServiceError] = mockService { _ =>
          Task.now(Left(ServiceFailed("some err")))
        }
      }

      val req = request(correctJsonString)

      val res = controller.create().apply(req)
      status(res) should equal(INTERNAL_SERVER_ERROR)
    }
  }

}

trait TestContext {
  def mockService[Req, Res, E <: ServiceError](fn: Req => Task[Either[E, Res]]): Service[Req, Res, E] = new Service[Req, Res, E] {
    def apply[Ctx: Context, P: Profiler, L: Logger](req: Req): Task[Either[E, Res]] = fn(req)
  }

  val uuid = UUID.randomUUID()

  def service: NotesService

  val scheduler = monix.execution.Scheduler.Implicits.global

  // has to be val as `service` is not initialized when TestContext constructor is being initialized
  lazy val controller = {
    implicit val profiler = NoopServiceProfiler
    implicit val controllerComponents = stubControllerComponents()

    new NotesController(service, scheduler)
  }
}
