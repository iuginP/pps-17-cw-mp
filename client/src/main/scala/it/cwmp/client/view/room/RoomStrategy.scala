package it.cwmp.client.view.room

/**
  * A strategy to know what to do when requested to create or enter a room
  *
  * @author Enrico Siboni
  * @author contributor Davide Borficchia
  */
trait RoomStrategy {

  /**
    * Invoked when user wants to create a room
    *
    * @param roomName      the room name
    * @param playersNumber the palyers number
    */
  def onCreate(roomName: String, playersNumber: Int): Unit

  /**
    * Invoked whe user wants to enter a private room
    *
    * @param roomID the room to enter
    */
  def onEnterPrivate(roomID: String): Unit

  /**
    * Invoked when the user wants to enter a public room
    *
    * @param playersNumber the public room players number
    */
  def onEnterPublic(playersNumber: Int): Unit

  /**
    * Invoked when the user wants to return to logIn view
    */
  def onClosingRoomView(): Unit
}
