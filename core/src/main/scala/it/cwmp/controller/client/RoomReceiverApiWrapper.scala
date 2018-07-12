package it.cwmp.controller.client

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
    * Sends the addresses to the client
    *
    * @param toSend the addresses to send
    * @return a Future that completes when the client received the data
    */
  def sendParticipantAddresses(clientAddress: String, toSend: Seq[Participant]): Future[Unit]

}

/**
  * Companion object
  */
object RoomReceiverApiWrapper {

  def API_RECEIVE_PARTICIPANTS_URL(token: String) = s"/api/client/$token/room/participants"

  val DEFAULT_PORT = 8668

  def apply(): RoomReceiverApiWrapper = RoomReceiverApiWrapperDefault()


  /**
    * Default implementation for client communication
    */
  private case class RoomReceiverApiWrapperDefault() extends RoomReceiverApiWrapper with ApiClient {

    override def sendParticipantAddresses(clientAddress: String, toSend: Seq[Participant]): Future[Unit] = {

      val addressesJSONArray = toSend.foldLeft(Json emptyArr()) { (jsonArray, participant) =>
        jsonArray add participant.toJson
      }

      import scala.concurrent.ExecutionContext.Implicits.global

      createWebClient()
        .post(clientAddress)
        .sendJsonFuture(addressesJSONArray)
        .map(_ => Unit)

      // should implement a retry strategy?
    }
  }

}