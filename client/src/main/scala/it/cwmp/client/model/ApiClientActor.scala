package it.cwmp.client.model

import akka.actor.Actor
import it.cwmp.client.controller.ClientControllerMessages
import it.cwmp.controller.rooms.RoomsApiWrapper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Questo oggetto contiene tutti i messaggi che questo attore può ricevere.
  */
object ApiClientIncomingMessages {

  /**
    * Questo messaggio rappresenta la visualizzazione dell'interfaccia grafica per la gestione delle lobby delle stanze.
    * Quando lo ricevuto, viene mostrata all'utente l'interfaccia grafica.
    *
    * @param name è il nome della stanza da creare
    * @param nPlayer è il numero dei giocatori che potranno entrare nella stanza
    */
  case class RoomCreatePrivate(name: String, nPlayer: Int, token: String)
}

/**
  * Questo oggetto contiene tutti i messaggi che questo attore può inviare.
  */
object ApiClientOutgoingMessages {

  case class RoomCreatePrivateSuccesful(token: String)
  case class RoomCreatePrivateFailure(reason: String)
}

import ApiClientIncomingMessages._
import ApiClientOutgoingMessages._

object ApiClientActor {
  def apply(): ApiClientActor = new ApiClientActor()
}

class ApiClientActor() extends Actor{

  /**
    * Possono essere ricevut imessaggi di tipo [[ApiClientIncomingMessages]] ed inviati quelli di tipo [[ApiClientOutgoingMessages]]
    * @return [[Receive]] che gestisce tutti i messaggi corrispondenti alle richieste che è possibile inviare ai servizi online
    */
  // Tutti i behaviour sono attivi in contemporanea, la separazione è solo logica per una migliore leggibilità
  override def receive: Receive = roomManagerBehaviour // orElse ...

  /*
   * Behaviour che gestisce tutte le chiamate al servizio di gestione delle stanze.
   */
  private val apiWrapper = RoomsApiWrapper()
  import apiWrapper._
  private def roomManagerBehaviour: Receive = {
    case RoomCreatePrivate(name, nPlayer, token) =>
      val senderTmp = sender
      createRoom(name, nPlayer)(token).onComplete({
        case Success(t) => senderTmp ! RoomCreatePrivateSuccesful(t)
        case Failure(reason) => senderTmp ! RoomCreatePrivateFailure(reason.getMessage)
      })
  }
}
