package it.cwmp.controller.client

import io.vertx.lang.scala.json.Json
import it.cwmp.controller.ApiClient
import it.cwmp.model.User

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
  def sendParticipantAddresses(clientAddress: String, toSend: Seq[String]): Future[Unit]

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
  private case class RoomReceiverApiWrapperDefault() extends RoomReceiverApiWrapper /*with ApiClient*/ {

    override def sendParticipantAddresses(clientAddress: String, toSend: Seq[String]): Future[Unit] = {
      // TODO: al ritorno da questa funzione in caso di errori non saranno effettuati altri tentaivi
      // TODO: questo è il punto in cui cercare di far ricevere i dati al client
      // in caso un client non sia più disponibile il server, passerà oltre, e gli altri client (se più di 1)
      // giocheranno tra di loro; il server non ha più responsabilità

      val addressesJSONArray = toSend.foldLeft(Json emptyArr()) { (jsonArray, address) =>
        jsonArray add Json.obj((User.FIELD_ADDRESS, address))
      }

      // need to know the format of address provided ti server to separate into port and host parts

      // TODO: send addresses to the client
      Future.successful(Unit)
    }
  }

}