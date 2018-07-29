package it.cwmp.client.controller.messages

import it.cwmp.model.Address

/**
  * The collection of Rooms request messages
  */
object RoomsRequests {

  /**
    * Create a new private room; request from GUI
    *
    * @param name          the room name
    * @param playersNumber the players number
    */
  sealed case class GUICreate(name: String, playersNumber: Int)

  /**
    * Enter a private room; request from GUI
    *
    * @param roomID the roomID to enter
    */
  sealed case class GUIEnterPrivate(roomID: String)

  /**
    * Enter a public room; request from GUI
    *
    * @param playersNumber the players number of the public room to enter
    */
  sealed case class GUIEnterPublic(playersNumber: Int)

  /**
    * Create a new private room; request for online service
    *
    * @param name          the name of the room
    * @param playersNumber the players number
    * @param token         the user token
    */
  sealed case class ServiceCreate(name: String, playersNumber: Int, token: String)

  /**
    * Enter a private room; request for online service
    *
    * @param roomID        the room id
    * @param playerAddress the player address that wants to join the room
    * @param webAddress    the address where tha player wants to receive other participants addresses
    * @param token         the user token
    */
  sealed case class ServiceEnterPrivate(roomID: String, playerAddress: Address, webAddress: Address, token: String)

  /**
    * Enter a public room; request for online service
    *
    * @param playersNumber the number of players the public room should have
    * @param playerAddress the player address that wants to join the room
    * @param webAddress    the address where the player wants to receive tha other participants addresses
    * @param token         the user token
    */
  sealed case class ServiceEnterPublic(playersNumber: Int, playerAddress: Address, webAddress: Address, token: String)


}

/**
  * The collection of Rooms response messages
  */
object RoomsResponses {

  /**
    * Creation successful
    *
    * @param roomID the room identifier to use entering the room
    */
  sealed case class CreateSuccess(roomID: String)

  /**
    * Creation failed
    *
    * @param errorMessage optionally an error message
    */
  sealed case class CreateFailure(errorMessage: Option[String])

  /**
    * Enter private room succeeded
    */
  case object EnterPrivateSuccess

  /**
    * Enter private room failed
    *
    * @param errorMessage optionally an error message
    */
  sealed case class EnterPrivateFailure(errorMessage: Option[String])

  /**
    * Enter public room succeeded
    */
  case object EnterPublicSuccess

  /**
    * Enter public room failed
    *
    * @param errorMessage optionally an error message
    */
  sealed case class EnterPublicFailure(errorMessage: Option[String])

}