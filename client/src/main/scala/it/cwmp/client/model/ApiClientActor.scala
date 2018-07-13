package it.cwmp.client.model

import akka.actor.Actor
import it.cwmp.controller.rooms.RoomsApiWrapper
import it.cwmp.model.Participant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Questo oggetto contiene tutti i messaggi che questo attore può ricevere.
  */
object ApiClientIncomingMessages {

  /**
    * Questo messaggio gestisce la volontà di creare una nuova stanza privata.
    * Quando lo ricevo, inoltro la richiesta al servizio online.
    *
    * @param name è il nome della stanza da creare
    * @param nPlayer è il numero dei giocatori che potranno entrare nella stanza
    * @param token è il token d'autenticazione per poter fare le richieste
    */
  case class RoomCreatePrivate(name: String, nPlayer: Int, token: String)

  /**
    * Questo messaggio gestisce la volontà di entrare in  una stanza privata.
    * Quando lo ricevo, inoltro la richiesta al servizio online.
    *
    * @param idRoom è l'id della stanza nella quale voglio entrare
    * @param participant è il partecipante che si vuole far entrare nella stanza
    * @param webAddress è l'indirizzo del web server in ascolto per la lista dei partecipanti
    * @param token è il token d'autenticazione per poter fare le richieste
    */
  case class RoomEnterPrivate(idRoom: String, participant: Participant, webAddress: String, token: String)

  /**
    * Questo messaggio gestisce la volontà di entrare in una stanza pubblica
    * Quando lo ricevo, inoltro la richiesta al servizio online.
    *
    * @param nPlayer è il numero dei partecipanti a quella stanza
    */
  case class RoomEnterPublic(nPlayer: Integer, participant: Participant, webAddress: String, token: String)
}

/**
  * Questo oggetto contiene tutti i messaggi che questo attore può inviare.
  */
object ApiClientOutgoingMessages {
  /**
    * Questo messaggio rappresenta il successo della crazione di una stanza privata.
    *
    * @param token è il token identificativo che restitusice il model dopo che la stanza viene creata correttamente
    */
  case class RoomCreatePrivateSuccesful(token: String)
  /**
    * Questo messaggio rappresenta il fallimento nella crazione di una stanza privata.
    *
    * @param reason è il motivo che ha generato il fallimento
    */
  case class RoomCreatePrivateFailure(reason: String)
  /**
    * Questo messaggio rappresenta che si è entrati in una stanza privata
    */
  case object RoomEnterPrivateSuccesful
  /**
    * Questo messaggio rappresenta che si è entrati in una stanza pubblica
    */
  case object  RoomEnterPublicSuccesful
  /**
    * Questo messaggio rappresenta che non si è entrati in una stanza pubblica
    */
  /**
    * Questo messaggio rappresenta il fallimento nella entrata in una stanza pubblica.
    *
    * @param reason è il motivo che ha generato il fallimento
    */
  case class RoomEnterPublicFailure(reason: String)

  /**
    * Questo messaggio rappresenta il fallimento quando si prova ad entrare in una stanza privata
    *
    * @param reason è il motivo del fallimento
    */
  case class RoomEnterPrivateFailure(reason: String)
}

import ApiClientIncomingMessages._
import ApiClientOutgoingMessages._

object ApiClientActor {
  def apply(): ApiClientActor = new ApiClientActor()
}

class ApiClientActor() extends Actor{

  /**
    * Possono essere ricevuti messaggi di tipo [[ApiClientIncomingMessages]] ed inviati quelli di tipo [[ApiClientOutgoingMessages]]
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
    case RoomEnterPrivate(idRoom, participant, webAddress, token) =>
      val senderTmp = sender
      enterRoom(idRoom)(participant, token).onComplete({ //todo utilizzare il vero PARTICIPANT
        case Success(_) => senderTmp ! RoomEnterPrivateSuccesful
        case Failure(error) => senderTmp ! RoomEnterPrivateFailure(error.getMessage)
      })
    case RoomEnterPublic(nPlayer, participant, webAddress, token) => //TODO serve webAddress
      val senderTmp = sender
      enterPublicRoom(nPlayer)(participant, token).onComplete({ //todo utilizzare il vero PARTICIPANT
        case Success(_) => senderTmp ! RoomEnterPublicSuccesful
        case Failure(error) => senderTmp ! RoomEnterPublicFailure(error.getMessage)
      })

  }
}
