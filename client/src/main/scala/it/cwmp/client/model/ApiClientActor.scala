package it.cwmp.client.model

import akka.actor.Actor
import it.cwmp.client.controller.ClientControllerMessages
import it.cwmp.controller.rooms.RoomsApiWrapper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Questo oggetto contiene tutti i messaggi che questo attore può ricevere.
  */
object ApiClientMessages {

  /**
    * Questo messaggio rappresenta la visualizzazione dell'interfaccia grafica per la gestione delle lobby delle stanze.
    * Quando lo ricevuto, viene mostrata all'utente l'interfaccia grafica.
    *
    * @param name è il nome della stanza da creare
    * @param nPlayer è il numero dei giocatori che potranno entrare nella stanza
    */
  case class RoomCreatePrivate(name: String, nPlayer: Int, token: String)
}

object ApiClientActor {
  def apply(): ApiClientActor = new ApiClientActor()
}

class ApiClientActor() extends Actor{

  def receive = roomManagerBehaviour

  private val apiWrapper = RoomsApiWrapper()
  import apiWrapper._
  def roomManagerBehaviour: Receive = {
    case ClientControllerMessages.RoomCreatePrivate(name, nPlayer, token) =>
      createRoom(name, nPlayer)(token).onComplete({
        case Success(s) => println(s)
        case Failure(e) => e.printStackTrace()
      })
  }

  /**
    * Imposta il behavior del [[ApiClientActor]] in modo da gestire solo la lobby delle stanze
    */
  def becomeRoomsManager(): Unit = {
    context.become(roomManagerBehaviour)
  }
}
