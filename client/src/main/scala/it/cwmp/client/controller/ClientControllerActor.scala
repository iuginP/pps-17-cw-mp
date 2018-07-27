package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.cwmp.client.model.PlayerActor._
import it.cwmp.client.model._
import it.cwmp.client.view.AlertMessages
import it.cwmp.client.view.authentication.{AuthenticationViewActor, AuthenticationViewMessages}
import it.cwmp.client.view.room.{RoomViewActor, RoomViewMessages}
import it.cwmp.model.{Address, Participant}
import it.cwmp.utils.Logging

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Questa classe rappresenta l'attore del controller del client che ha il compito
  * di fare da tramite tra le view e i model.
  *
  * @param system è l'[[ActorSystem]] che ospita gli attori che dovranno comunicare tra di loro
  * @author Davide Borficchia
  * @author Eugenio Pierfederici
  */
case class ClientControllerActor(system: ActorSystem) extends Actor with ParticipantListReceiver with Logging {

  private val UNKNOWN_ERROR = "Unknown Error"

  var jwtToken: String = _

  /**
    * Questo attore è quello che si occupa di gestire la partita di gioco.
    * Sono questi attori, per ciascun client, a connettersi nel cluster e gestire lo svolgimento del gioco.
    */
  var playerActor: ActorRef = _
  var playerAddress: String = _

  /**
    * Questo attore si occupa di effettuare tutte le richieste ai servizi web e attendere le risposte.
    */
  var apiClientActor: ActorRef = _

  /**
    * Questo è l'attore che gestisce la view della lebboy delle stanze al quale invieremo i messaggi
    */
  var roomViewActor: ActorRef = _

  /**
    * Actor for the management of authentication processes to which the relative messages will be sent.
    */
  var authenticationViewActor: ActorRef = _

  /**
    * Questa metodo non va richiamato manualmente ma viene chiamato in automatico
    * quando viene creato l'attore [[ClientControllerActor]].
    * Il suo compito è quello di creare l'attore [[RoomViewActor]].
    * Una volta creato inizializza e mostra la GUI
    */
  override def preStart(): Unit = {
    super.preStart()
    // Initialize all actors
    log.info(s"Initializing the player actor...")
    playerActor = system.actorOf(Props(classOf[PlayerActor], system), "player")
    playerActor ! RetrieveAddress
    log.info(s"Initializing the API client actor...")
    apiClientActor = system.actorOf(Props[ApiClientActor], "roomAPIClient") //todo parametrizzare le stringhe
    log.info(s"Initializing the authentication view actor...")
    authenticationViewActor = system.actorOf(Props[AuthenticationViewActor], "authenticationView")
    authenticationViewActor ! AuthenticationViewMessages.InitController
    log.info(s"Initializing the room view actor...")
    roomViewActor = system.actorOf(Props[RoomViewActor], "roomView")
    roomViewActor ! RoomViewMessages.InitController

    log.info(s"Displaying the view...")
    authenticationViewActor ! AuthenticationViewMessages.ShowGUI
  }

  /**
    * Questa metodo gestisce tutti i possibili behavior che può assumero l'attore [[ClientControllerActor]].
    * Un behavior è un subset di azioni che il controller può eseguire in un determianto momento .
    */
  override def receive: Receive = apiClientReceiverBehaviour orElse authenticationManagerBehaviour orElse {
    case RetrieveAddressResponse(address) =>
      playerAddress = address
  }

  /**
    * Set the behavior of the [[ClientControllerActor]] in order to handle authentication processes
    */
  def becomeAuthenticationManager(): Unit = {
    log.info(s"Setting the behaviour 'authentication-manager'")
    context.become(apiClientReceiverBehaviour orElse authenticationManagerBehaviour)
  }

  /**
    * Imposta il behavior del [[ClientControllerActor]] in modo da gestire solo la lobby delle stanze
    */
  private def becomeRoomsManager(): Unit = {
    log.info(s"Setting the behaviour 'room-manager'")
    context.become(apiClientReceiverBehaviour orElse roomManagerBehaviour)
    roomViewActor ! RoomViewMessages.ShowGUI
  }

  private def becomeInGame(participants: List[Participant]): Unit = {
    log.info(s"Setting the behaviour 'in-game'")
    context.become(inGameBehaviour)
    roomViewActor ! RoomViewMessages.HideGUI
    playerActor ! StartGame(participants)
  }


  import it.cwmp.client.controller.ClientControllerMessages._

  /**
    * Behavior to be applied to manage authentication processes.
    * Messages that can be processed in this behavior are shown in [[ClientControllerMessages]]
    *
    */
  def authenticationManagerBehaviour: Receive = {
    case AuthenticationPerformSignIn(username, password) =>
      log.info(s"Signing in as $username")
      apiClientActor ! ApiClientIncomingMessages.AuthenticationPerformSignIn(username, password)
    case AuthenticationPerformSignUp(username, password) =>
      log.info(s"Signing up as $username")
      apiClientActor ! ApiClientIncomingMessages.AuthenticationPerformSignUp(username, password)
  }

  /**
    * Questo metodo rappresenta il behavior che si ha quando si sta gestendo la lobby delle stanze.
    * I messaggi che questo attore, in questo behavoir, è ingrado di ricevere sono raggruppati in [[ClientControllerMessages]]
    *
    */

  import it.cwmp.client.controller.ClientControllerMessages._

  private def roomManagerBehaviour: Receive = {
    case RoomCreatePrivate(name, nPlayer) =>
      log.info(s"Creating the room $name")
      apiClientActor ! ApiClientIncomingMessages.RoomCreatePrivate(name, nPlayer, jwtToken)
    case RoomEnterPrivate(idRoom) =>
      log.info(s"Entering the private room $idRoom")
      enterRoom().map(url =>
        apiClientActor ! ApiClientIncomingMessages.RoomEnterPrivate(
          idRoom, Address(playerAddress), url, jwtToken)
      )
    case RoomEnterPublic(nPlayer) =>
      log.info(s"Entering the public room with $nPlayer players")
      enterRoom().map(url =>
        apiClientActor ! ApiClientIncomingMessages.RoomEnterPublic(
          nPlayer, Address(playerAddress), url, jwtToken)
      )
  }

  private def inGameBehaviour: Receive = {
    case _ => // TODO
  }

  private def enterRoom(): Future[Address] = {
    log.debug(s"Starting the local one-time reception server...")
    // Apre il server in ricezione per la lista dei partecipanti
    listenForParticipantListFuture(
      // Quando ha ricevuto la lista dei partecipanti dal server
      participants => {
        log.info(s"Participants list received!")
        becomeInGame(participants)
      }
    ).andThen({ // Una volta creato
      case Success(address) =>
        log.debug(s"Server completely started listening at the address: $address")
      case Failure(error) => // Invia un messaggio di errore alla GUI
        log.error(s"Problem starting the server: ${error.getMessage}")
        roomViewActor ! AlertMessages.Error("Error", error.getMessage)
    })
  }

  /**
    * Questo è il behavior che sta in ascolto del successo o meno di una chiamata fatta ad un servizio online tramite l'ApiClientActor.
    * I messaggi che questo attore, in questo behavoir, è in grado di ricevere sono raggruppati in [[ApiClientOutgoingMessages]]
    *
    */

  import ApiClientOutgoingMessages._

  // TODO: qui lasciamo il behaviuor misto?
  private def apiClientReceiverBehaviour: Receive = {
    case AuthenticationSignInSuccessful(token) =>
        this.jwtToken = token
        becomeRoomsManager()
        authenticationViewActor ! AuthenticationViewMessages.HideGUI
    case AuthenticationSignInFailure(reason) =>
      authenticationViewActor ! AlertMessages.Error("Warning", reason.getOrElse(UNKNOWN_ERROR))
    case AuthenticationSignUpSuccessful(token) =>
        this.jwtToken = token
        becomeRoomsManager()
        authenticationViewActor ! AuthenticationViewMessages.HideGUI
    case AuthenticationSignUpFailure(reason) =>
      authenticationViewActor ! AlertMessages.Error("Warning", reason.getOrElse(UNKNOWN_ERROR))

    case RoomCreatePrivateSuccessful(token) =>
      roomViewActor ! AlertMessages.Info("Token", token)
    case RoomCreatePrivateFailure(reason) =>
      roomViewActor ! AlertMessages.Error("Problem", reason.getOrElse(UNKNOWN_ERROR)) // TODO parametrizzazione stringhe
    case RoomEnterPrivateSuccessful =>
      //roomViewActor ! AlertMessages.Info("Stanza privata", "Sei entrato") // TODO parametrizzazione stringhe
    case RoomEnterPrivateFailure(reason) =>
      roomViewActor ! AlertMessages.Error("Problem", reason.getOrElse(UNKNOWN_ERROR)) // TODO parametrizzazione stringhe
    case RoomEnterPublicSuccessful =>
      //roomViewActor ! AlertMessages.Info("Stanza pubblica", "Sei entrato") // TODO parametrizzazione stringhe
    case RoomEnterPublicFailure(reason) =>
      roomViewActor ! AlertMessages.Error("Problem", reason.getOrElse(UNKNOWN_ERROR)) // TODO parametrizzazione stringhe
  }
}

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
  case class RoomEnterPublic(nPlayer: Int)

}