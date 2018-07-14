package it.cwmp.controller.client

import io.vertx.core.http.HttpMethod
import io.vertx.lang.scala.json.Json
import it.cwmp.controller.ApiClient
import it.cwmp.model.Participant
import it.cwmp.model.Participant.Converters._

import scala.concurrent.Future

/**
  * A trait that describes the communication that the server has to have with clients
  *
  * @author Enrico Siboni
  */
trait RoomReceiverApiWrapper {

  /**
    * Sends the participants to the client
    *
    * @param toSend the participants to send
    * @return a Future that completes when the client received the data
    */
  def sendParticipants(clientAddress: String, toSend: Seq[Participant]): Future[Unit]

}

/**
  * Companion object
  */
object RoomReceiverApiWrapper {

  def API_RECEIVE_PARTICIPANTS_URL(token: String) = s"/api/client/$token/room/participants"

  def apply(): RoomReceiverApiWrapper = RoomReceiverApiWrapperDefault()


  /**
    * Default implementation for client communication
    */
  private case class RoomReceiverApiWrapperDefault() extends RoomReceiverApiWrapper with ApiClient {

    override def sendParticipants(clientAddress: String, toSend: Seq[Participant]): Future[Unit] = {
      import scala.concurrent.ExecutionContext.Implicits.global
      createWebClient()
        .requestAbs(HttpMethod.POST, clientAddress)
        .sendJsonFuture(toSend.foldLeft(Json emptyArr())(_ add _.toJson))
        .map(_ => Unit)

      // TODO should implement a retry strategy?
    }
  }

}