package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.cwmp.client.controller.ClientControllerMessages._
import it.cwmp.client.model.ApiClientOutgoingMessages._
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
  * This class is client controller actor, that will manage interactions between client parts
  *
  * @param system the system of this actors
  * @author Davide Borficchia
  * @author Eugenio Pierfederici
  */
case class ClientControllerActor(system: ActorSystem) extends Actor with ParticipantListReceiver with Logging {

  private val UNKNOWN_ERROR = "Unknown Error"

  /**
    * This actor manages the game.
    * Those actors for each client connect together in a cluster and manage game developing
    */
  private var playerActor: ActorRef = _
  private var playerAddress: String = _

  /**
    * This actor manages all requests to web services
    */
  private var apiClientActor: ActorRef = _

  /**
    * Actor for the management of authentication processes to which the relative messages will be sent.
    */
  private var authenticationViewActor: ActorRef = _

  /**
    * This actor manages the view of rooms
    */
  private var roomViewActor: ActorRef = _

  private var jwtToken: String = _

  override def preStart(): Unit = {
    super.preStart()

    log.info(s"Initializing the player actor...")
    playerActor = system.actorOf(Props(classOf[PlayerActor], system), PlayerActor.getClass.getName)
    playerActor ! RetrieveAddress

    log.info(s"Initializing the API client actor...")
    apiClientActor = system.actorOf(Props[ApiClientActor], ApiClientActor.getClass.getName)

    log.info(s"Initializing the authentication view actor...")
    authenticationViewActor = system.actorOf(Props[AuthenticationViewActor], AuthenticationViewActor.getClass.getName)
    authenticationViewActor ! AuthenticationViewMessages.InitController

    log.info(s"Displaying the view...")
    authenticationViewActor ! AuthenticationViewMessages.ShowGUI

    log.info(s"Initializing the room view actor...")
    roomViewActor = system.actorOf(Props[RoomViewActor], RoomViewActor.getClass.getName)
    roomViewActor ! RoomViewMessages.InitController
  }

  override def receive: Receive = playerAddressRetrievalBehaviour

  /**
    * @return the behaviour of retrieving address of player actor
    */
  private def playerAddressRetrievalBehaviour: Receive = {
    case RetrieveAddressResponse(address) =>
      playerAddress = address
      context.become(authenticationApiReceiverBehaviour orElse authenticationGUIBehaviour)
  }

  /**
    * @return the behaviour that manages authentication commands from GUI
    */
  private def authenticationGUIBehaviour: Receive = {
    case AuthenticationPerformSignIn(username, password) =>
      log.info(s"Signing in as $username")
      apiClientActor ! ApiClientIncomingMessages.AuthenticationPerformSignIn(username, password)
    case AuthenticationPerformSignUp(username, password) =>
      log.info(s"Signing up as $username") // TODO: group together same name messages
      apiClientActor ! ApiClientIncomingMessages.AuthenticationPerformSignUp(username, password)
  }

  /**
    * @return the behaviour that manages authentication API responses
    */
  private def authenticationApiReceiverBehaviour: Receive = {
    case AuthenticationSignInSuccessful(token) =>
      jwtToken = token
      onSuccessfulLogin()
      authenticationViewActor ! AuthenticationViewMessages.HideGUI
    case AuthenticationSignInFailure(reason) =>
      authenticationViewActor ! AlertMessages.Error("Warning", reason.getOrElse(UNKNOWN_ERROR))
    case AuthenticationSignUpSuccessful(token) =>
      jwtToken = token
      onSuccessfulLogin()
      authenticationViewActor ! AuthenticationViewMessages.HideGUI
    case AuthenticationSignUpFailure(reason) =>
      authenticationViewActor ! AlertMessages.Error("Warning", reason.getOrElse(UNKNOWN_ERROR))
  }

  /**
    * @return the behaviour that manages rooms commands from GUI
    */
  private def roomsGUIBehaviour: Receive = {
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

  /**
    * @return the behaviour that manages room API responses
    */
  private def roomsApiReceiverBehaviour: Receive = {
    case RoomCreatePrivateSuccessful(token) =>
      roomViewActor ! RoomViewMessages.ShowToken("Token", token)
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
        onSuccessFindingOpponents(participants)
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
    * Action to execute when logout occurs
    */
  private def onLogOut(): Unit = {
    log.info(s"Setting the behaviour 'authentication-manager'")
    context.become(authenticationApiReceiverBehaviour orElse authenticationGUIBehaviour)
    authenticationViewActor ! AuthenticationViewMessages.ShowGUI
  }

  /**
    * Action to do on successful login
    */
  private def onSuccessfulLogin(): Unit = {
    log.info(s"Setting the behaviour 'room-manager'")
    context.become(roomsApiReceiverBehaviour orElse roomsGUIBehaviour)
    roomViewActor ! RoomViewMessages.ShowGUI
  }

  /**
    * Action to execute when found opponents
    *
    * @param participants the participants to game
    */
  private def onSuccessFindingOpponents(participants: List[Participant]): Unit = {
    log.info(s"Setting the behaviour 'in-game'")
    context.become(inGameBehaviour)
    roomViewActor ! RoomViewMessages.HideGUI // TODO: why here hideGUI and in auth no?
    playerActor ! StartGame(participants)
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