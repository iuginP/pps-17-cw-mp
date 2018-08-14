package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, Props}
import it.cwmp.client.controller.AlertMessages.{Error, Info}
import it.cwmp.client.controller.ClientControllerActor._
import it.cwmp.client.controller.PlayerActor.{PrepareForGame, RetrieveAddress, RetrieveAddressResponse}
import it.cwmp.client.controller.ViewVisibilityMessages.{Hide, Show}
import it.cwmp.client.controller.game.{CellWorldGenerationStrategy, GameConstants}
import it.cwmp.client.controller.messages.AuthenticationRequests.{GUILogOut, LogIn, SignUp}
import it.cwmp.client.controller.messages.AuthenticationResponses.{LogInFailure, LogInSuccess, SignUpFailure, SignUpSuccess}
import it.cwmp.client.controller.messages.Initialize
import it.cwmp.client.controller.messages.RoomsRequests._
import it.cwmp.client.controller.messages.RoomsResponses._
import it.cwmp.client.view.authentication.AuthenticationViewActor
import it.cwmp.client.view.room.RoomViewActor
import it.cwmp.client.view.room.RoomViewActor.{FoundOpponents, ShowToken, WaitingForOthers}
import it.cwmp.model.{Address, Participant}
import it.cwmp.utils.Logging

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * This class is client controller actor, that will manage interactions between client parts
  *
  * @param apiClientActor the actor to use to contact the remote services
  * @author Davide Borficchia
  * @author Eugenio Pierfederici
  * @author contributor Enrico Siboni
  */
case class ClientControllerActor(private val apiClientActor: ActorRef) extends Actor with ParticipantListReceiver with Logging {

  private val UNKNOWN_ERROR = "Unknown Error"

  /**
    * This actor manages the game.
    * Those actors for each client connect together in a cluster and manage game developing
    */
  private var playerActor: ActorRef = _
  private var playerAddress: String = _

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
    playerActor = context.system.actorOf(Props[PlayerActor], PlayerActor.getClass.getName)
    playerActor ! RetrieveAddress

    log.info(s"Initializing the authentication view actor...")
    authenticationViewActor = context.system.actorOf(Props[AuthenticationViewActor], AuthenticationViewActor.getClass.getName)
    authenticationViewActor ! Initialize

    log.info(s"Displaying the view...")
    authenticationViewActor ! Show

    log.info(s"Initializing the room view actor...")
    roomViewActor = context.system.actorOf(Props[RoomViewActor], RoomViewActor.getClass.getName)
    roomViewActor ! Initialize
  }

  override def receive: Receive = playerAddressRetrievalBehaviour

  /**
    * @return the behaviour of retrieving address of player actor
    */
  private def playerAddressRetrievalBehaviour: Receive = {
    case RetrieveAddressResponse(address) =>
      playerAddress = address
      context.become(authenticationGUIBehaviour orElse authenticationApiReceiverBehaviour)
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
    * Action to do on successful authentication
    */
  private def onAuthenticationSuccess(token: String): Unit = {
    authenticationViewActor ! Info(AUTHENTICATION_SUCCEEDED_TITLE, AUTHENTICATION_SUCCEEDED_MESSAGE)
    jwtToken = token
    setRoomBehaviourAndShowRoomsView()
    authenticationViewActor ! Hide
  }

  /**
    * Changes behaviour to listen for rooms commands and shows room GUI
    */
  private def setRoomBehaviourAndShowRoomsView(): Unit = {
    log.info(s"Setting the behaviour 'room-manager'")
    context.become(roomsGUIBehaviour orElse roomsApiReceiverBehaviour)
    roomViewActor ! Show
  }

  /**
    * Action to do on failed authentication
    *
    * @param errorMessage optionally the error message
    */
  private def onAuthenticationFailure(errorMessage: Option[String]): Unit =
    authenticationViewActor ! Error(AUTHENTICATION_ERROR_TITLE, errorMessage.getOrElse(UNKNOWN_ERROR))

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
    case GUIExitPrivate(roomID) =>
      log.info(s"Exiting room $roomID")
      apiClientActor ! ServiceExitPrivate(roomID, jwtToken)
    case GUIExitPublic(playersNumber) =>
      log.info(s"Exiting public room with $playersNumber players")
      apiClientActor ! ServiceExitPublic(playersNumber, jwtToken)
    case GUILogOut =>
      log.info("Logging-out")
      onLogOut()
  }

  /**
    * @return the behaviour that manages room API responses
    */
  // scalastyle:off cyclomatic.complexity
  private def roomsApiReceiverBehaviour: Receive = {
    case CreateSuccess(token) => roomViewActor ! ShowToken(token)
    case CreateFailure(errorMessage) => roomViewActor ! Error(CREATE_ERROR_TITLE, errorMessage.getOrElse(UNKNOWN_ERROR))

    case EnterPrivateSuccess => onRoomEnteringSuccess()
    case EnterPublicSuccess => onRoomEnteringSuccess()
    case EnterPrivateFailure(errorMessage) => onRoomEnteringFailure(errorMessage)
    case EnterPublicFailure(errorMessage) => onRoomEnteringFailure(errorMessage)

    case ExitPrivateSuccess => onRoomExitingSuccess()
    case ExitPublicSuccess => onRoomExitingSuccess()
    case ExitPrivateFailure(errorMessage) => onRoomExitingFailure(errorMessage)
    case ExitPublicFailure(errorMessage) => onRoomExitingFailure(errorMessage)
  }

  // scalastyle:on cyclomatic.complexity

  /**
    * Action to execute when logout occurs
    */
  private def onLogOut(): Unit = {
    roomViewActor ! Hide
    setAuthenticationBehaviourAndShowGUI()
  }

  /**
    * Sets the authentication behaviour and shows the authentication GUI
    */
  private def setAuthenticationBehaviourAndShowGUI(): Unit = {
    log.info(s"Setting the behaviour 'authentication-manager'")
    context.become(authenticationGUIBehaviour orElse authenticationApiReceiverBehaviour)
    authenticationViewActor ! Show
  }

  /**
    * Action to do on successful room entering
    */
  private def onRoomEnteringSuccess(): Unit = {
    roomViewActor ! Info(ROOM_ENTERING_SUCCEEDED_TITLE, ROOM_ENTERING_SUCCEEDED_MESSAGE)
    roomViewActor ! WaitingForOthers
  }

  /**
    * Action to do on room entering failure
    *
    * @param errorMessage optionally the error message
    */
  private def onRoomEnteringFailure(errorMessage: Option[String]): Unit = {
    stopListeningForParticipants()
    roomViewActor ! Error(ENTERING_ERROR_TITLE, errorMessage.getOrElse(UNKNOWN_ERROR))
  }

  /**
    * Action to do on room exiting success
    */
  private def onRoomExitingSuccess(): Unit = {
    log.info("Succeeded exiting from the room, now stopping one-time server participant receiver")
    stopListeningForParticipants()
  }

  /**
    * Action to do on room exiting failure
    *
    * @param errorMessage optionally an error message
    */
  private def onRoomExitingFailure(errorMessage: Option[String]): Unit = {
    log.warn("Failed exiting the room, maybe participant number reached while trying to exit")
    log.error(s"${errorMessage.getOrElse(UNKNOWN_ERROR)}")
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
      .andThen {
        case Success(address) =>
          log.debug(s"Server started and listening at the address: $address")
        case Failure(error) =>
          log.error(s"Problem starting the server: ${error.getMessage}")
          roomViewActor ! Error(RECEIVING_PARTICIPANT_LIST_ERROR_TITLE, error.getMessage)
      }
  }

  /**
    * Action to execute when found opponents
    *
    * @param participants the participants to game
    */
  private def onSuccessFindingOpponents(participants: List[Participant]): Unit = {
    roomViewActor ! FoundOpponents
    log.info(s"Setting the behaviour 'in-game'")
    context.become(Actor.emptyBehavior)
    roomViewActor ! Hide
    playerActor ! PrepareForGame(participants,
      CellWorldGenerationStrategy(GameViewActor.VIEW_SIZE, GameViewActor.VIEW_SIZE, GameConstants.PASSIVE_CELLS_NUMBER))
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

  private val AUTHENTICATION_SUCCEEDED_TITLE = "Autenticazione riuscita!"
  private val AUTHENTICATION_SUCCEEDED_MESSAGE = "Ti sei autenticato correttamente :)"

  private val ROOM_ENTERING_SUCCEEDED_TITLE = "Entrata nella stanza riuscita!"
  private val ROOM_ENTERING_SUCCEEDED_MESSAGE = "Sei entrato nella stanza, ora attendiamo altri partecipanti!"
}
