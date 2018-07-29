package it.cwmp.client.view.room

import it.cwmp.client.controller.messages.RoomsRequests.{GUICreate, GUIEnterPrivate, GUIEnterPublic}
import it.cwmp.client.view.FXServiceViewActor
import it.cwmp.client.view.room.RoomViewActor.{CREATING_PRIVATE_ROOM_MESSAGE, ENTERING_ROOM_MESSAGE, ENTERING_ROOM_TITLE, ShowToken}

/**
  * This class represents the actor that manages the rooms
  *
  * @author Davide Borficchia
  * @author contributor Enrico Siboni
  */
case class RoomViewActor() extends FXServiceViewActor {

  protected var fxController: RoomFXController = _

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
          fxController showLoading(ENTERING_ROOM_MESSAGE, ENTERING_ROOM_TITLE) // TODO: use cancellable loading, and test
          controllerActor ! GUIEnterPrivate(roomID)
        }

        override def onEnterPublic(playersNumber: Int): Unit = {
          fxController disableViewComponents()
          fxController showLoading(ENTERING_ROOM_MESSAGE, ENTERING_ROOM_TITLE) // TODO: use cancellable loading, and test
          controllerActor ! GUIEnterPublic(playersNumber)
        }
      }))
  }

  override def receive: Receive = super.receive orElse {
    case ShowToken(roomToken) => runOnUIThread(() => {
      onServiceResponseReceived()
      fxController showTokenDialog roomToken // TODO: make possible to close dialogs whit X (already done... but check)
    })
  }
}

/**
  * Companion object, with actor messages
  */
object RoomViewActor {

  private val CREATING_PRIVATE_ROOM_MESSAGE = "Stiamo creando la stanza privata"

  private val ENTERING_ROOM_TITLE = "In attesa di giocatori"
  private val ENTERING_ROOM_MESSAGE = "Stai per entrare nella stanza scelta"

  /**
    * Shows the room token on screen
    *
    * @param roomToken the private room token to spread among friends
    */
  case class ShowToken(roomToken: String)

}