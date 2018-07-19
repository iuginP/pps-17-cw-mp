package it.cwmp.services.roomreceiver

import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.client.WebClientOptions
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.Participant.Converters._
import it.cwmp.services.roomreceiver.ServerParameters._
import it.cwmp.services.testing.roomreceiver.RoomReceiverWebTesting
import it.cwmp.testing.{FutureMatchers, HttpMatchers}
import it.cwmp.utils.VertxClient

import scala.util.{Failure, Success}

class RoomReceiverServiceVerticleTest extends RoomReceiverWebTesting
  with HttpMatchers with FutureMatchers with VertxClient {

  override protected val clientOptions: WebClientOptions = WebClientOptions()
    .setDefaultHost("localhost")
    .setKeepAlive(false)

  describe("RoomReceiver") {
    describe("Receiving partecipants list") {
      it("When wrong url should 404") {
        client.post(API_RECEIVE_PARTICIPANTS_URL(wrongToken)).port(port)
          .sendFuture()
          .map(res => res statusCode() should equal(404))
      }

      it("When right url and empty body should 400") {
        client.post(API_RECEIVE_PARTICIPANTS_URL(rightToken)).port(port)
          .sendFuture()
          .map(res => res statusCode() should equal(400))
      }

      it("When right url and body should 201") {
        client.post(API_RECEIVE_PARTICIPANTS_URL(rightToken)).port(port)
          .sendJsonFuture(participants.foldLeft(Json emptyArr())(_ add _.toJson))
          .map(res => res statusCode() should equal(201))
      }

      it("When right url and body should obtain the correct list") {
        client.post(API_RECEIVE_PARTICIPANTS_URL(rightToken)).port(port)
          .sendJsonFuture(participants.foldLeft(Json emptyArr())(_ add _.toJson))
          .flatMap(_ => participantsPromise.future)
          .map(l => l should equal(participants))
      }

      it("When right, after response should close") {
        client.post(API_RECEIVE_PARTICIPANTS_URL(rightToken)).port(port)
          .sendFuture()
          .transform({
            case Success(res) if res.statusCode() == 201 => Success(Unit)
            case Success(res) => Failure(HTTPException(res.statusCode())) // TODO: add an error message as second argument of HTTP exception
            case Failure(f) => Failure(f)
          })
          .flatMap(_ => client.post(API_RECEIVE_PARTICIPANTS_URL(rightToken))
            .sendFuture()
            .map(_ => fail))
          .shouldFailWith[HTTPException]
      }
    }
  }

}
