package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.cwmp.client.controller.AlertMessages.Error
import it.cwmp.client.controller.ClientControllerActor.{AUTHENTICATION_ERROR_TITLE, CREATE_ERROR_TITLE, ENTERING_ERROR_TITLE, RECEIVING_PARTICIPANT_LIST_ERROR_TITLE}
import it.cwmp.client.controller.PlayerActor.{RetrieveAddress, RetrieveAddressResponse, StartGame}
import it.cwmp.client.controller.ViewVisibilityMessages.{Hide, Show}
import it.cwmp.client.controller.messages.AuthenticationRequests.{LogIn, SignUp}
import it.cwmp.client.controller.messages.AuthenticationResponses.{LogInFailure, LogInSuccess, SignUpFailure, SignUpSuccess}
import it.cwmp.client.controller.messages.Initialize
import it.cwmp.client.controller.messages.RoomsRequests._
import it.cwmp.client.controller.messages.RoomsResponses._
import it.cwmp.client.view.authentication.AuthenticationViewActor
import it.cwmp.client.view.room.RoomViewActor
import it.cwmp.client.view.room.RoomViewActor.ShowToken
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
  * @author contributor Enrico Siboni
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
    authenticationViewActor ! Initialize

    log.info(s"Displaying the view...")
    authenticationViewActor ! Show

    log.info(s"Initializing the room view actor...")
    roomViewActor = system.actorOf(Props[RoomViewActor], RoomViewActor.getClass.getName)
    roomViewActor ! Initialize
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
    case GUICreate(roomName, playersNumber) =>
      log.info(s"Creating the room $roomName")
      apiClientActor ! ServiceCreate(roomName, playersNumber, jwtToken)
    case GUIEnterPrivate(roomID) =>
      log.info(s"Entering the private room $roomID")
      openOneTimeServerAndGetAddress()
        .map(url => apiClientActor ! ServiceEnterPrivate(roomID, Address(playerAddress), url, jwtToken))
    case GUIEnterPublic(playersNumber) =>
      log.info(s"Entering the public room with $playersNumber players")
      openOneTimeServerAndGetAddress()
        .map(url => apiClientActor ! ServiceEnterPublic(playersNumber, Address(playerAddress), url, jwtToken))
  }

  /**
    * @return the behaviour that manages room API responses
    */
  private def roomsApiReceiverBehaviour: Receive = {
    case CreateSuccess(token) => roomViewActor ! ShowToken(token)
    case CreateFailure(errorMessage) => roomViewActor ! Error(CREATE_ERROR_TITLE, errorMessage.getOrElse(UNKNOWN_ERROR))
    case EnterPrivateSuccess => //roomViewActor ! AlertMessages.Info("Stanza privata", "Sei entrato") todo review this behaviour (now to hide loading it leverages on rooms participant receiving
    case EnterPrivateFailure(errorMessage) => roomViewActor ! Error(ENTERING_ERROR_TITLE, errorMessage.getOrElse(UNKNOWN_ERROR))
    case EnterPublicSuccess => //roomViewActor ! AlertMessages.Info("Stanza pubblica", "Sei entrato") todo review this behaviour
    case EnterPublicFailure(errorMessage) => roomViewActor ! Error(ENTERING_ERROR_TITLE, errorMessage.getOrElse(UNKNOWN_ERROR))
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
          roomViewActor ! Error(RECEIVING_PARTICIPANT_LIST_ERROR_TITLE, error.getMessage)
      })
  }

  /**
    * Action to execute when logout occurs
    */
  private def onLogOut(): Unit = {
    log.info(s"Setting the behaviour 'authentication-manager'")
    context.become(authenticationApiReceiverBehaviour orElse authenticationGUIBehaviour)
    authenticationViewActor ! Show
  }

  /**
    * Action to do on successful authentication
    */
  private def onAuthenticationSuccess(token: String): Unit = {
    jwtToken = token
    log.info(s"Setting the behaviour 'room-manager'")
    context.become(roomsApiReceiverBehaviour orElse roomsGUIBehaviour)
    roomViewActor ! Show
    authenticationViewActor ! Hide
  }

  /**
    * Action to do on failed authentication
    */
  private def onAuthenticationFailure(errorMessage: Option[String]): Unit =
    authenticationViewActor ! Error(AUTHENTICATION_ERROR_TITLE, errorMessage.getOrElse(UNKNOWN_ERROR))

  /**
    * Action to execute when found opponents
    *
    * @param participants the participants to game
    */
  private def onSuccessFindingOpponents(participants: List[Participant]): Unit = {
    log.info(s"Setting the behaviour 'in-game'")
    context.become(inGameBehaviour)
    roomViewActor ! Hide
    playerActor ! StartGame(participants)
  }
}

/**
  * Companion object
  */
object ClientControllerActor {
  private val CREATE_ERROR_TITLE = "Errore nella creazione della stanza"
  private val ENTERING_ERROR_TITLE = "Errore nell'entrata nella stanza"
  private val RECEIVING_PARTICIPANT_LIST_ERROR_TITLE = "Errore durante la ricezione degli altri partecipanti"
  private val AUTHENTICATION_ERROR_TITLE = "Errore durante l'autenticazione"
}