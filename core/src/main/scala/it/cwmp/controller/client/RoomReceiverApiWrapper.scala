package it.cwmp.controller.client

import com.typesafe.scalalogging.Logger
import io.vertx.lang.scala.json.Json
import it.cwmp.controller.ApiClient
import it.cwmp.model.Participant
import it.cwmp.model.Participant.Converters._

import scala.concurrent.Future

/**
  * A trait that describes the communication that the server has to have with clients
  *
  * @author Enrico Siboni
  * @author Eugenio Pierfederici
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
  private val logger: Logger = Logger[RoomReceiverApiWrapper]

  def API_RECEIVE_PARTICIPANTS_URL(token: String) = s"/api/client/$token/room/participants"

  val DEFAULT_PORT = 8668

  def apply(): RoomReceiverApiWrapper = RoomReceiverApiWrapperDefault()


  /**
    * Default implementation for client communication
    */
  private case class RoomReceiverApiWrapperDefault() extends RoomReceiverApiWrapper with ApiClient {

    override def sendParticipants(clientAddress: String, toSend: Seq[Participant]): Future[Unit] = {
      import scala.concurrent.ExecutionContext.Implicits.global
      createWebClient()
        .post(clientAddress)
        .sendJsonFuture(toSend.foldLeft(Json emptyArr())(_ add _.toJson))
        .map(_ => logger.info(s"Sending participant to $clientAddress succeeded"))

      // TODO should implement a retry strategy?
    }
  }

}