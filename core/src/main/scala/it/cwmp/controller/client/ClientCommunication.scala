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
    * @param addresses the addresses to send
    * @return a Future that completes when the client received the data
    */
  def sendParticipantAddresses(addresses: Seq[String]): Future[Unit]

}

/**
  * Companion object
  */
object ClientCommunication {

  def apply(url: String): ClientCommunication = ClientCommunicationDefault(url)


  /**
    * Default implementation for client communication
    *
    * @param clientUrl the url at which to find the client
    */
  private case class ClientCommunicationDefault(clientUrl: String) extends ClientCommunication {

    // TODO: use web client?? maybe a refactoring of all those api wrappers should be done in a general ApiWrapper

    override def sendParticipantAddresses(addresses: Seq[String]): Future[Unit] = {
      // TODO: qui bisogna assicurarsi che il client riceva i dati, al ritorno da questa funzione in caso di errori non saranno effettuati altri tentaivi

      val addressesJSONArray = addresses.foldLeft(Json emptyArr()) { (jsonArray, address) =>
        jsonArray add Json.obj((User.FIELD_ADDRESS, address))
      }

      // TODO: send addresses to the client
      Future.successful(Unit)
    }
  }

}