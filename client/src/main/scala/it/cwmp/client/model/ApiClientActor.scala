package it.cwmp.client.model

import akka.actor.Actor
import it.cwmp.client.controller.messages.AuthenticationRequests.{LogIn, SignUp}
import it.cwmp.client.controller.messages.AuthenticationResponses.{LogInFailure, LogInSuccess, SignUpFailure, SignUpSuccess}
import it.cwmp.model.Address
import it.cwmp.services.wrapper.{AuthenticationApiWrapper, RoomsApiWrapper}

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
    * @param name    è il nome della stanza da creare
    * @param nPlayer è il numero dei giocatori che potranno entrare nella stanza
    * @param token   è il token d'autenticazione per poter fare le richieste
    */
  case class RoomCreatePrivate(name: String, nPlayer: Int, token: String)

  /**
    * Questo messaggio gestisce la volontà di entrare in  una stanza privata.
    * Quando lo ricevo, inoltro la richiesta al servizio online.
    *
    * @param idRoom        è l'id della stanza nella quale voglio entrare
    * @param playerAddress è il partecipante che si vuole far entrare nella stanza
    * @param webAddress    è l'indirizzo del web server in ascolto per la lista dei partecipanti
    * @param token         è il token d'autenticazione per poter fare le richieste
    */
  case class RoomEnterPrivate(idRoom: String, playerAddress: Address, webAddress: Address, token: String)

  /**
    * Questo messaggio gestisce la volontà di entrare in una stanza pubblica
    * Quando lo ricevo, inoltro la richiesta al servizio online.
    *
    * @param nPlayer è il numero dei partecipanti a quella stanza
    */
  case class RoomEnterPublic(nPlayer: Int, playerAddress: Address, webAddress: Address, token: String)

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
  case class RoomCreatePrivateSuccessful(token: String)

  /**
    * Questo messaggio rappresenta il fallimento nella crazione di una stanza privata.
    *
    * @param reason è il motivo che ha generato il fallimento
    */
  case class RoomCreatePrivateFailure(reason: Option[String])

  /**
    * Questo messaggio rappresenta che si è entrati in una stanza privata
    */
  case object RoomEnterPrivateSuccessful

  /**
    * Questo messaggio rappresenta che si è entrati in una stanza pubblica
    */
  case object RoomEnterPublicSuccessful

  /**
    * Questo messaggio rappresenta che non si è entrati in una stanza pubblica
    */
  /**
    * Questo messaggio rappresenta il fallimento nella entrata in una stanza pubblica.
    *
    * @param reason è il motivo che ha generato il fallimento
    */
  case class RoomEnterPublicFailure(reason: Option[String])

  /**
    * Questo messaggio rappresenta il fallimento quando si prova ad entrare in una stanza privata
    *
    * @param reason è il motivo del fallimento
    */
  case class RoomEnterPrivateFailure(reason: Option[String])

}

import it.cwmp.client.model.ApiClientIncomingMessages._
import it.cwmp.client.model.ApiClientOutgoingMessages._

object ApiClientActor {
  def apply(): ApiClientActor = new ApiClientActor()
}

class ApiClientActor() extends Actor {

  /**
    * Possono essere ricevuti messaggi di tipo [[ApiClientIncomingMessages]] ed inviati quelli di tipo [[ApiClientOutgoingMessages]]
    *
    * @return [[Receive]] che gestisce tutti i messaggi corrispondenti alle richieste che è possibile inviare ai servizi online
    */
  // Tutti i behaviour sono attivi in contemporanea, la separazione è solo logica per una migliore leggibilità
  override def receive: Receive = authenticationBehaviour orElse roomManagerBehaviour // orElse ...

  /*
   * Behaviour che gestisce tutte le chiamate al servizio di gestione delle stanze.
   */
  private val roomApiWrapper = RoomsApiWrapper()

  import roomApiWrapper._

  private def roomManagerBehaviour: Receive = {
    case RoomCreatePrivate(name, nPlayer, token) =>
      val senderTmp = sender
      createRoom(name, nPlayer)(token).onComplete({
        case Success(t) => senderTmp ! RoomCreatePrivateSuccessful(t)
        case Failure(error) => senderTmp ! RoomCreatePrivateFailure(Option(error.getMessage))
      })
    case RoomEnterPrivate(idRoom, address, webAddress, token) =>
      val senderTmp = sender
      enterRoom(idRoom, address, webAddress)(token).onComplete({
        case Success(_) => senderTmp ! RoomEnterPrivateSuccessful
        case Failure(error) => senderTmp ! RoomEnterPrivateFailure(Option(error.getMessage))
      })
    case RoomEnterPublic(nPlayer, address, webAddress, token) =>
      val senderTmp = sender
      enterPublicRoom(nPlayer, address, webAddress)(token).onComplete({
        case Success(_) => senderTmp ! RoomEnterPublicSuccessful
        case Failure(error) => senderTmp ! RoomEnterPublicFailure(Option(error.getMessage))
      })
  }

  /**
    * @return the behaviour of authenticating user online
    */
  private def authenticationBehaviour: Receive = {
    val authenticationApiWrapper = AuthenticationApiWrapper()
    //noinspection ScalaStyle
    import authenticationApiWrapper._
    {
      case LogIn(username, password) =>
        val senderTmp = sender
        login(username, password).onComplete {
          case Success(token) => senderTmp ! LogInSuccess(token)
          case Failure(ex) => senderTmp ! LogInFailure(Option(ex.getMessage))
        }
      case SignUp(username, password) =>
        val senderTmp = sender
        signUp(username, password).onComplete({
          case Success(token) => senderTmp ! SignUpSuccess(token)
          case Failure(ex) => senderTmp ! SignUpFailure(Option(ex.getMessage))
        })
    }
  }
}
