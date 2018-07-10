package it.cwmp.controller.client

import io.vertx.lang.scala.json.Json
import it.cwmp.model.User

import scala.concurrent.Future

/**
  * A trait that describes the communication that the server has to have with clients
  *
  * @author Enrico Siboni
  */
trait ClientCommunication {

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
object ClientCommunication {

  def apply(): ClientCommunication = ClientCommunicationDefault()


  /**
    * Default implementation for client communication
    */
  private case class ClientCommunicationDefault() extends ClientCommunication /*with ApiClient*/ {

    override def sendParticipantAddresses(clientAddress: String, toSend: Seq[String]): Future[Unit] = {
      // TODO: al ritorno da questa funzione in caso di errori non saranno effettuati altri tentaivi
      // TODO: questo è il punto in cui cercare di far ricevere i dati al client
      // in caso un client non sia più disponibile il server, passerà oltre, e gli altri client (se più di 1)
      // giocheranno tra di loro; il server non ha più responsabilità

      val addressesJSONArray = toSend.foldLeft(Json emptyArr()) { (jsonArray, address) =>
        jsonArray add Json.obj((User.FIELD_ADDRESS, address))
      }

      // TODO: send addresses to the client
      Future.successful(Unit)
    }
  }

}