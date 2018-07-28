package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.cwmp.client.controller.ClientControllerMessages._
import it.cwmp.client.controller.PlayerActor.{RetrieveAddress, RetrieveAddressResponse, StartGame}
import it.cwmp.client.controller.messages.AuthenticationRequests.{LogIn, SignUp}
import it.cwmp.client.controller.messages.AuthenticationResponses.{LogInFailure, LogInSuccess, SignUpFailure, SignUpSuccess}
import it.cwmp.client.controller.messages.RoomsRequests.{Create, EnterPrivate, EnterPublic}
import it.cwmp.client.controller.messages.RoomsResponses._
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
    case message@LogIn(username, _) =>
      log.info(s"Signing in as $username")
      apiClientActor ! message
    case message@SignUp(username, _) =>
      log.info(s"Signing up as $username")
      apiClientActor ! message
  }

  /**
    * @return the behaviour that manages authentication API responses
    */
  private def authenticationApiReceiverBehaviour: Receive = {
    case LogInSuccess(token) => onAuthenticationSuccess(token)
    case SignUpSuccess(token) => onAuthenticationSuccess(token)
    case LogInFailure(errorMessage) => onAuthenticationFailure(errorMessage)
    case SignUpFailure(errorMessage) => onAuthenticationFailure(errorMessage)
  }

  /**
    * @return the behaviour that manages rooms commands from GUI
    */
  private def roomsGUIBehaviour: Receive = {
    case RoomCreatePrivate(name, nPlayer) =>
      log.info(s"Creating the room $name")
      apiClientActor ! Create(name, nPlayer, jwtToken)
    case RoomEnterPrivate(idRoom) =>
      log.info(s"Entering the private room $idRoom")
      openOneTimeServerAndGetAddress()
        .map(url => apiClientActor ! EnterPrivate(idRoom, Address(playerAddress), url, jwtToken))
    case RoomEnterPublic(nPlayer) =>
      log.info(s"Entering the public room with $nPlayer players")
      openOneTimeServerAndGetAddress()
        .map(url => apiClientActor ! EnterPublic(nPlayer, Address(playerAddress), url, jwtToken)
        )
  }

  /**
    * @return the behaviour that manages room API responses
    */
  private def roomsApiReceiverBehaviour: Receive = {
    case CreateSuccess(token) => roomViewActor ! RoomViewMessages.ShowToken("Token", token)
    case CreateFailure(reason) => roomViewActor ! AlertMessages.Error("Problem", reason.getOrElse(UNKNOWN_ERROR)) // TODO parametrizzazione stringhe
    case EnterPrivateSuccess => //roomViewActor ! AlertMessages.Info("Stanza privata", "Sei entrato") // TODO parametrizzazione stringhe
    case EnterPrivateFailure(reason) => roomViewActor ! AlertMessages.Error("Problem", reason.getOrElse(UNKNOWN_ERROR)) // TODO parametrizzazione stringhe
    case EnterPublicSuccess => //roomViewActor ! AlertMessages.Info("Stanza pubblica", "Sei entrato") // TODO parametrizzazione stringhe
    case EnterPublicFailure(reason) => roomViewActor ! AlertMessages.Error("Problem", reason.getOrElse(UNKNOWN_ERROR)) // TODO parametrizzazione stringhe
  }

  private def inGameBehaviour: Receive = {
    case _ => // TODO
  }

  /**
    * @return the Future containing the address of one-time server that will receive the participants
    */
  private def openOneTimeServerAndGetAddress(): Future[Address] = {
    val onListReceived: List[Participant] => Unit = participants => {
      log.info(s"Participants list received!")
      onSuccessFindingOpponents(participants)
    }

    log.debug(s"Starting the local one-time receiver server...")
    listenForParticipantListFuture(onListReceived)
      .andThen({
        case Success(address) =>
          log.debug(s"Server started and listening at the address: $address")
        case Failure(error) =>
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
    * Action to do on successful authentication
    */
  private def onAuthenticationSuccess(token: String): Unit = {
    jwtToken = token
    log.info(s"Setting the behaviour 'room-manager'")
    context.become(roomsApiReceiverBehaviour orElse roomsGUIBehaviour)
    roomViewActor ! RoomViewMessages.ShowGUI
    authenticationViewActor ! AuthenticationViewMessages.HideGUI
  }

  /**
    * Action to do on failed authentication
    */
  private def onAuthenticationFailure(errorMessage: Option[String]): Unit =
    authenticationViewActor ! AlertMessages.Error("Warning", errorMessage.getOrElse(UNKNOWN_ERROR))

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