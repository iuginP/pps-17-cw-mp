package it.cwmp.roomreceiver.controller

import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.controller.client.RoomReceiverApiWrapper
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.Participant
import it.cwmp.testing.VertxTest
import org.scalatest.{BeforeAndAfterEach, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success}

class RoomReceiverServiceVerticleTest extends VertxTest with BeforeAndAfterEach with Matchers {

  private val token = "barabba"
  private val wrongToken = "rabarbaro"
  private val list = Participant("Nome", "Indirizzo") :: Participant("Nome1", "Indirizzo1") :: List[Participant]()
  private var promiseList: Promise[Seq[Participant]] = _

  private def client = {
    val options = WebClientOptions()
      .setDefaultHost("localhost")
      .setDefaultPort(RoomReceiverApiWrapper.DEFAULT_PORT)
      .setKeepAlive(false)
    WebClient.create(vertx, options)
  }

  private var deploymentID: String = _

  override protected def beforeEach(): Unit = {
    promiseList = Promise()
    deploymentID = Await.result(vertx.deployVerticleFuture(RoomReceiverServiceVerticle(token, s => promiseList.success(s))), 10000.millis)
  }

  override protected def afterEach(): Unit =
    Await.result(vertx.undeployFuture(deploymentID), 10000.millis)

  describe("RoomReceiver") {
    describe("Receiving partecipants list") {
      it("When wrong url should 404") {
        client.post(RoomReceiverApiWrapper.API_RECEIVE_PARTICIPANTS_URL(wrongToken))
          .sendFuture()
          .map(res => res statusCode() should equal(404))
      }

      it("When right url and empty body should 400") {
        client.post(RoomReceiverApiWrapper.API_RECEIVE_PARTICIPANTS_URL(token))
          .sendFuture()
          .map(res => res statusCode() should equal(400))
      }

      it("When right url and body should 201") {
        client.post(RoomReceiverApiWrapper.API_RECEIVE_PARTICIPANTS_URL(token))
          .sendJsonFuture(list.foldLeft(Json emptyArr())(_ add _.toJson))
          .map(res => res statusCode() should equal(201))
      }

      it("When right url and body should obtain the correct list") {
        client.post(RoomReceiverApiWrapper.API_RECEIVE_PARTICIPANTS_URL(token))
          .sendJsonFuture(list.foldLeft(Json emptyArr())(_ add _.toJson))
          .flatMap(_ => promiseList.future)
          .map(l => l should equal(list))
      }

      it("When right, after response should close") {
        recoverToSucceededIf[HTTPException] {
          client.post(RoomReceiverApiWrapper.API_RECEIVE_PARTICIPANTS_URL(token))
            .sendFuture()
            .transform({
              case Success(res) if res.statusCode() == 201 => Success(Unit)
              case Success(res) => Failure(HTTPException(res.statusCode()))// TODO: add an error message as second argument of HTTP exception
              case Failure(f) => Failure(f)
            })
            .flatMap(_ => client.post(RoomReceiverApiWrapper.API_RECEIVE_PARTICIPANTS_URL(token))
              .sendFuture()
              .map(_ => fail))
        }
      }
    }
  }

}
