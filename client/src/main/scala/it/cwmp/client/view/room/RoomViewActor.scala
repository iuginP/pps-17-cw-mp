package it.cwmp.client.view.room

import it.cwmp.client.controller.messages.RoomsRequests.{GUICreate, GUIEnterPrivate, GUIEnterPublic}
import it.cwmp.client.view.FXViewActor
import it.cwmp.client.view.room.RoomViewActor.ShowToken

/**
  * This class represents the actor that manages the rooms
  *
  * @author Davide Borficchia
  */
case class RoomViewActor() extends FXViewActor {

  protected var fxController: RoomFXController = _

  override def preStart(): Unit = {
    super.preStart()
    runOnUIThread(() =>
      fxController = RoomFXController(new RoomStrategy {
        override def onCreate(roomName: String, playersNumber: Int): Unit =
          controllerActor ! GUICreate(roomName, playersNumber)

        override def onEnterPrivate(roomID: String): Unit =
          controllerActor ! GUIEnterPrivate(roomID)

        override def onEnterPublic(playersNumber: Int): Unit =
          controllerActor ! GUIEnterPublic(playersNumber)
      }))
  }

  override def receive: Receive = super.receive orElse {
    case ShowToken(roomToken) => runOnUIThread(() => {
      //      onAlertReceived() was called before moving all to super class
      fxController showTokenDialog roomToken // TODO: make possible to close dialogs whit X
    })
  }
}

/**
  * Companion object, with actor messages
  */
object RoomViewActor {

  /**
    * Shows the room token on screen
    *
    * @param roomToken the private room token to spread among friends
    */
  case class ShowToken(roomToken: String)

}