package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.cwmp.client.model._
import it.cwmp.client.view.AlertMessages
import it.cwmp.client.view.room.{RoomViewActor, RoomViewMessages}
import it.cwmp.model.Participant

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Questo oggetto contiene tutti i messaggi che questo attore può ricevere.
  */
object ClientControllerMessages {

  /**
    * Questo messaggio gestisce la volontà di creare una nuova stanza privata.
    * Quando lo ricevo, invio la richiesta all'attore che gestisce i servizi online delle stanze.
    *
    * @param name è il nome della stanza da creare
    * @param nPlayer è il numero dei giocatori che potranno entrare nella stanza
    */
  case class RoomCreatePrivate(name: String, nPlayer: Int)
  /**
    * Questo messaggio gestisce la volontà di entrare in una stanza privata.
    * Quando lo ricevo, invio la richiesta all'attore che gestisce i servizi online delle stanze.
    *
    * @param idRoom è l'id che identifica la stanza privata
    */
  case class RoomEnterPrivate(idRoom: String)
}

object ClientControllerActor {
  def apply(system: ActorSystem): ClientControllerActor = new ClientControllerActor(system)
}

/**
  * Questa classe rappresenta l'attore del controller del client che ha il compito
  * di fare da tramite tra le view e i model.
  *
  * @param system è l'[[ActorSystem]] che ospita gli attori che dovranno comunicare tra di loro
  *
  * @author Davide Borficchia
  */
class ClientControllerActor(system: ActorSystem) extends Actor with ParticipantListReceiver {

  // TODO debug token
  val username = "pippo"
  val jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InBpcHBvIn0.jPVT_3dOaioA7480e0q0lwdUjExe7Di5tixdZCsQQD4"

  /**
    * Questo attore è quello che si occupa di gestire la partita di gioco.
    * Sono questi attori, per ciascun client, a connettersi nel cluster e gestire lo svolgimento del gioco.
    */
  var playerActor: ActorRef = _
  /**
    * Questo è l'attore che gestisce la view della lebboy delle stanze al quale invieremo i messaggi
    */
  var roomViewActor: ActorRef = _
  var roomApiClientActor: ActorRef = _

  /**
    * Questa metodo non va richiamato manualmente ma viene chiamato in automatico
    * quando viene creato l'attore [[ClientControllerActor]].
    * Il suo compito è quello di creare l'attore [[RoomViewActor]].
    * Una volta creato inizializza e mostra la GUI
    */
  override def preStart(): Unit = {
    super.preStart()
    // Initialize all actors
    playerActor = system.actorOf(Props[PlayerActor], "player")
    roomApiClientActor = system.actorOf(Props[ApiClientActor], "roomAPIClient") //todo parametrizzare le stringhe
    roomViewActor = system.actorOf(Props[RoomViewActor], "roomView")
    roomViewActor ! RoomViewMessages.InitController
    // TODO debug, remove before release
    roomViewActor ! RoomViewMessages.ShowGUI
  }

  /**
    * Questa metodo gestisce tutti i possibili behavior che può assumero l'attore [[ClientControllerActor]].
    * Un behavior è un subset di azioni che il controller può eseguire in un determianto momento .
    */
  override def receive: Receive = apiClientReceiverBehaviour orElse roomManagerBehaviour

  /**
    * Imposta il behavior del [[ClientControllerActor]] in modo da gestire solo la lobby delle stanze
    */
  private def becomeRoomsManager(): Unit = {
    context.become(apiClientReceiverBehaviour orElse roomManagerBehaviour)
  }

  /**
    * Questo metodo rappresenta il behavior che si ha quando si sta gestendo la lobby delle stanze.
    * I messaggi che questo attore, in questo behavoir, è ingrado di ricevere sono raggruppati in [[ClientControllerMessages]]
    *
    */
  import it.cwmp.client.controller.ClientControllerMessages._
  private def roomManagerBehaviour: Receive = {
    case RoomCreatePrivate(name, nPlayer) =>
      roomApiClientActor ! ApiClientIncomingMessages.RoomCreatePrivate(name, nPlayer, jwtToken)
    case RoomEnterPrivate(idRoom) =>
      // Apre il server in ricezione per la lista dei partecipanti
      listenForParticipantListFuture(
        // Quando ha ricevuto la lista dei partecipanti dal server
        participants => playerActor ! PlayerIncomingMessages.StartGame(participants)
      ).onComplete({ // Una volta creato
          case Success(url) => // Richiede all'api actor di entrare nella stanza
            roomApiClientActor ! ApiClientIncomingMessages.RoomEnterPrivate(
              idRoom, Participant(username, playerActor.path.address.toString), url, jwtToken)
          case Failure(error) => // Invia un messaggio di errore alla GUI
            roomViewActor ! AlertMessages.Error("Error", error.getMessage)
        })
  }

  /**
    * Questo è il behavior che sta in ascolto del successo o meno di una chiamata fatta ad un servizio online tramite l'ApiClientActor.
    * I messaggi che questo attore, in questo behavoir, è in grado di ricevere sono raggruppati in [[ApiClientOutgoingMessages]]
    *
    */
  import ApiClientOutgoingMessages._
  private def apiClientReceiverBehaviour: Receive = {
    case RoomCreatePrivateSuccesful(token) =>
      roomViewActor ! AlertMessages.Info("Token", token)
    case RoomCreatePrivateFailure(reason) => AlertMessages.Error("Problem", reason) // TODO parametrizzazione stringhe
    case RoomEnterPrivateSuccesful => roomViewActor ! AlertMessages.Info("Stanza privata", "Sei entrato") // TODO parametrizzazione stringhe
    case RoomEnterPrivateFailure(reason) => AlertMessages.Error("Problem", reason) // TODO parametrizzazione stringhe
  }
}