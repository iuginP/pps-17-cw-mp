package it.cwmp.client.view.room

import it.cwmp.client.controller.messages.AuthenticationRequests.GUILogOut
import it.cwmp.client.controller.messages.RoomsRequests._
import it.cwmp.client.view.FXServiceViewActor
import it.cwmp.client.view.room.RoomViewActor._

/**
  * This class represents the actor that manages the rooms
  *
  * @author Davide Borficchia
  * @author contributor Enrico Siboni
  */
case class RoomViewActor() extends FXServiceViewActor {

  protected var fxController: RoomFXController = _

  private var roomEnteringMessage: RoomEnteringRequest with GUIRequest = _

  override def preStart(): Unit = {
    super.preStart()
    runOnUIThread(() =>
      fxController = RoomFXController(new RoomStrategy {
        override def onCreate(roomName: String, playersNumber: Int): Unit = {
          fxController disableViewComponents()
          fxController showLoading CREATING_PRIVATE_ROOM_MESSAGE
          controllerActor ! GUICreate(roomName, playersNumber)
        }

        override def onEnterPrivate(roomID: String): Unit = {
          fxController disableViewComponents()
          fxController showLoading(ENTERING_ROOM_MESSAGE, ENTERING_ROOM_TITLE)
          roomEnteringMessage = GUIEnterPrivate(roomID)
          controllerActor ! roomEnteringMessage
        }

        override def onEnterPublic(playersNumber: Int): Unit = {
          fxController disableViewComponents()
          fxController showLoading(ENTERING_ROOM_MESSAGE, ENTERING_ROOM_TITLE)
          roomEnteringMessage = GUIEnterPublic(playersNumber)
          controllerActor ! roomEnteringMessage
        }

        override def onClosingRoomView(): Unit = {
          controllerActor ! GUILogOut
        }
      }))
  }

  override def receive: Receive = super.receive orElse {
    case ShowToken(roomToken) =>
      onServiceResponseReceived()
      fxController showTokenDialog roomToken

    case FoundOpponents => onServiceResponseReceived()

    case WaitingForOthers =>
      fxController showCloseableLoading(WAITING_FOR_PARTICIPANTS_MESSAGE, WAITING_FOR_PARTICIPANTS_TITLE, () => {
        controllerActor ! (roomEnteringMessage match {
          case GUIEnterPrivate(roomID) => GUIExitPrivate(roomID)
          case GUIEnterPublic(playersNumber) => GUIExitPublic(playersNumber)
        })
      })
  }
}

/**
  * Companion object, with actor messages
  */
object RoomViewActor {

  private val CREATING_PRIVATE_ROOM_MESSAGE = "Stiamo creando la stanza privata"

  private val ENTERING_ROOM_TITLE = "Entrata in stanza"
  private val ENTERING_ROOM_MESSAGE = "Stai per entrare nella stanza scelta"

  private val WAITING_FOR_PARTICIPANTS_TITLE = "In attesa di giocatori"
  private val WAITING_FOR_PARTICIPANTS_MESSAGE = "Stiamo attendendo che altri giocatori si uniscano alla stessa stanza per raggiungere il numero stabilito"

  /**
    * Shows the room token on screen
    *
    * @param roomToken the private room token to spread among friends
    */
  case class ShowToken(roomToken: String)

  /**
    * Shows a loading while waiting for others participants
    */
  case object WaitingForOthers

  /**
    * Tells View actor that opponents have been found
    */
  case object FoundOpponents

}