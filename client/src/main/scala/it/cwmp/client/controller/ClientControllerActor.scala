package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.cwmp.client.model._
import it.cwmp.client.view.AlertMessages
import it.cwmp.client.view.authentication.{AuthenticationViewActor, AuthenticationViewMessages}
import it.cwmp.client.view.room.{RoomViewActor, RoomViewMessages}
import it.cwmp.model.Participant

import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Questo oggetto contiene tutti i messaggi che questo attore può ricevere.
  */
object ClientControllerMessages {

  /**
    * Message indicating the need to log into the system.
    * When the system receives it, it sends the request to the authentication online service.
    *
    * @param username identification chosen by the player to access the system
    * @param password password chosen during sign up
    */
  case class AuthenticationPerformSignIn(username: String, password: String)

  /**
    * Message indicating the need to create a new account.
    * When the system receives it, it sends the request to the authentication online service.
    *
    * @param username identification chosen by the player to register in the system
    * @param password password chosen to authenticate in the system
    */
  case class AuthenticationPerformSignUp(username: String, password: String)

  /**
    * Questo messaggio gestisce la volontà di creare una nuova stanza privata.
    * Quando lo ricevo, invio la richiesta all'attore che gestisce i servizi online delle stanze.
    *
    * @param name    è il nome della stanza da creare
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

  /**
    * Questo messaggio gestisce la volontà di entrare in una stanza pubblica.
    * Quando lo ricevo, invio la richiesta all'attore che gestisce i servizi online delle stanze.
    *
    * @param nPlayer è il numero dei partecipanti con i quali si vuole giocare
    */
  case class RoomEnterPublic(nPlayer: Integer)

}

object ClientControllerActor {
  def apply(system: ActorSystem): ClientControllerActor = new ClientControllerActor(system)
}

/**
  * Questa classe rappresenta l'attore del controller del client che ha il compito
  * di fare da tramite tra le view e i model.
  *
  * @param system è l'[[ActorSystem]] che ospita gli attori che dovranno comunicare tra di loro
  * @author Davide Borficchia
  */
class ClientControllerActor(system: ActorSystem) extends Actor with ParticipantListReceiver {

  // TODO debug token
  val debugUsername = "pippo"
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
    * Actor for the management of authentication processes to which the relative messages will be sent.
    */
  var authenticationViewActor: ActorRef = _
  // TODO: averli separati?
  var authenticationApiClientActor: ActorRef = _

  /**
    * Questa metodo non va richiamato manualmente ma viene chiamato in automatico
    * quando viene creato l'attore [[ClientControllerActor]].
    * Il suo compito è quello di creare l'attore [[RoomViewActor]].
    * Una volta creato inizializza e mostra la GUI
    */
  override def preStart(): Unit = {
    super.preStart()

    authenticationApiClientActor = system.actorOf(Props[ApiClientActor], "authenticationAPIClient")
    authenticationViewActor = system.actorOf(Props[AuthenticationViewActor], "authenticationView")
    authenticationViewActor ! AuthenticationViewMessages.InitController
    // TODO debug, remove before release
    authenticationViewActor ! AuthenticationViewMessages.ShowGUI

    // Initialize all actors
    /*playerActor = system.actorOf(Props[PlayerActor], "player")
    roomApiClientActor = system.actorOf(Props[ApiClientActor], "roomAPIClient") //todo parametrizzare le stringhe
    roomViewActor = system.actorOf(Props[RoomViewActor], "roomView")
    roomViewActor ! RoomViewMessages.InitController
    // TODO debug, remove before release
    roomViewActor ! RoomViewMessages.ShowGUI*/
  }

  /**
    * Questa metodo gestisce tutti i possibili behavior che può assumero l'attore [[ClientControllerActor]].
    * Un behavior è un subset di azioni che il controller può eseguire in un determianto momento .
    */
  // TODO: vanno tutti in orElse?
  override def receive: Receive = apiClientReceiverBehaviour orElse authenticationManagerBehaviour orElse roomManagerBehaviour

  /**
    * Set the behavior of the [[ClientControllerActor]] in order to handle authentication processes
    */
  def becomeAuthenticationManager(): Unit = {
    context.become(authenticationManagerBehaviour)
  }

  /**
    * Imposta il behavior del [[ClientControllerActor]] in modo da gestire solo la lobby delle stanze
    */
  private def becomeRoomsManager(): Unit = {
    context.become(apiClientReceiverBehaviour orElse roomManagerBehaviour)
  }


  import it.cwmp.client.controller.ClientControllerMessages._

  /**
    * Behavior to be applied to manage authentication processes.
    * Messages that can be processed in this behavior are shown in [[ClientControllerMessages]]
    *
    */
  def authenticationManagerBehaviour: Receive = {
    case AuthenticationPerformSignIn(username, password) =>
      authenticationApiClientActor ! ApiClientIncomingMessages.AuthenticationPerformSignIn(username, password)
    case AuthenticationPerformSignUp(username, password) =>
      authenticationApiClientActor ! ApiClientIncomingMessages.AuthenticationPerformSignUp(username, password)
  }

  /**
    * Questo metodo rappresenta il behavior che si ha quando si sta gestendo la lobby delle stanze.
    * I messaggi che questo attore, in questo behavoir, è ingrado di ricevere sono raggruppati in [[ClientControllerMessages]]
    *
    */
  private def roomManagerBehaviour: Receive = {
    case RoomCreatePrivate(name, nPlayer) =>
      roomApiClientActor ! ApiClientIncomingMessages.RoomCreatePrivate(name, nPlayer, jwtToken)
    case RoomEnterPrivate(idRoom) =>
      enterRoom().map(url =>
        roomApiClientActor ! ApiClientIncomingMessages.RoomEnterPrivate(
          idRoom, Participant(debugUsername, playerActor.path.address.toString), url, jwtToken)
      )
    case RoomEnterPublic(nPlayer) =>
      enterRoom().map(url =>
        roomApiClientActor ! ApiClientIncomingMessages.RoomEnterPublic(
          nPlayer, Participant(debugUsername, playerActor.path.address.toString), url, jwtToken)
      )
  }

  private def enterRoom(): Future[String] = {
    // Apre il server in ricezione per la lista dei partecipanti
    listenForParticipantListFuture(
      // Quando ha ricevuto la lista dei partecipanti dal server
      participants => playerActor ! PlayerIncomingMessages.StartGame(participants)
    ).andThen({ // Una volta creato
      case Failure(error) => // Invia un messaggio di errore alla GUI
        roomViewActor ! AlertMessages.Error("Error", error.getMessage)
    })
  }

  /**
    * Questo è il behavior che sta in ascolto del successo o meno di una chiamata fatta ad un servizio online tramite l'ApiClientActor.
    * I messaggi che questo attore, in questo behavoir, è in grado di ricevere sono raggruppati in [[ApiClientOutgoingMessages]]
    *
    */

  import it.cwmp.client.model.ApiClientOutgoingMessages._

  // TODO: qui lasciamo il behaviuor misto?
  // TODO: dall'altra parte non gestiamo la cosa?
  private def apiClientReceiverBehaviour: Receive = {
    case AuthenticationSignInSuccessful(token) =>
      authenticationViewActor ! AlertMessages.Info(s"Sign in", s"You're in! Token: $token")
    case AuthenticationSignInFailure(reason) =>
      authenticationViewActor ! AlertMessages.Error("Warning", reason)
    case AuthenticationSignUpSuccessful(token) =>
      authenticationViewActor ! AlertMessages.Info(s"Sign up", s"You're registered! Token: $token")
    case AuthenticationSignUpFailure(reason) =>
      authenticationViewActor ! AlertMessages.Error("Warning", reason)

    case RoomCreatePrivateSuccessful(token) =>
      roomViewActor ! AlertMessages.Info("Token", token)
    case RoomCreatePrivateFailure(reason) =>
      roomViewActor ! AlertMessages.Error("Problem", reason) // TODO parametrizzazione stringhe
    case RoomEnterPrivateSuccessful =>
      roomViewActor ! AlertMessages.Info("Stanza privata", "Sei entrato") // TODO parametrizzazione stringhe
    case RoomEnterPrivateFailure(reason) =>
      roomViewActor ! AlertMessages.Error("Problem", reason) // TODO parametrizzazione stringhe
    case RoomEnterPublicSuccessful =>
      roomViewActor ! AlertMessages.Info("Stanza pubblica", "Sei entrato") // TODO parametrizzazione stringhe
    case RoomEnterPublicFailure(reason) =>
      roomViewActor ! AlertMessages.Error("Problem", reason) // TODO parametrizzazione stringhe
  }
}