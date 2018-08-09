package it.cwmp.services.roomreceiver

import io.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, CREATED, NOT_FOUND}
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.client.WebClientOptions
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.Participant.Converters._
import it.cwmp.services.VertxClient
import it.cwmp.services.roomreceiver.ServerParameters._
import it.cwmp.services.testing.roomreceiver.RoomReceiverWebTesting
import it.cwmp.testing.{FutureMatchers, HttpMatchers}
import it.cwmp.utils.Utils.httpStatusNameToCode

import scala.util.{Failure, Success}

/**
  * A test class for Room receiver service
  */
class RoomReceiverServiceVerticleTest extends RoomReceiverWebTesting
  with HttpMatchers with FutureMatchers with VertxClient {

  override protected val clientOptions: WebClientOptions = WebClientOptions()
    .setDefaultHost("localhost")
    .setKeepAlive(false)

  describe("RoomReceiver") {
    describe("Receiving participants list") {
      it("When wrong url should NOT_FOUND") {
        client.post(createParticipantReceiverUrl(wrongToken)).port(port)
          .sendFuture() shouldAnswerWith NOT_FOUND
      }

      it("When right url and empty body should BAD_REQUEST") {
        client.post(createParticipantReceiverUrl(rightToken)).port(port)
          .sendFuture() shouldAnswerWith BAD_REQUEST
      }

      it("When right url and body should CREATED") {
        client.post(createParticipantReceiverUrl(rightToken)).port(port)
          .sendJsonFuture(participants.foldLeft(Json emptyArr())(_ add _.toJson))
          .shouldAnswerWith(CREATED)
      }

      it("When right url and body should obtain the correct list") {
        client.post(createParticipantReceiverUrl(rightToken)).port(port)
          .sendJsonFuture(participants.foldLeft(Json emptyArr())(_ add _.toJson))
          .flatMap(_ => participantsPromise.future)
          .map(_ should equal(participants))
      }

      it("When right, after response should close") {
        client.post(createParticipantReceiverUrl(rightToken)).port(port)
          .sendFuture()
          .transform({
            case Success(res) if res.statusCode() == CREATED.code() => Success(())
            case Success(res) => Failure(HTTPException(res.statusCode(), res.statusMessage()))
            case failure@Failure(_) => failure
          })
          .flatMap(_ => client.post(createParticipantReceiverUrl(rightToken))
            .sendFuture()
            .map(_ => fail))
          .shouldFailWith[HTTPException]
      }
    }
  }

}
