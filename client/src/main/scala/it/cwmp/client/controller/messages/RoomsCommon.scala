package it.cwmp.client.controller.messages

import it.cwmp.model.Address

/**
  * The collection of Rooms request messages
  */
object RoomsRequests {

  /**
    * Create a new private room
    *
    * @param name          the name of the room
    * @param playersNumber the players number
    * @param token         the user token
    */
  sealed case class Create(name: String, playersNumber: Int, token: String)

  /**
    * Enter a private room
    *
    * @param roomID        the room id
    * @param playerAddress the player address that wants to join the room
    * @param webAddress    the address where tha player wants to receive other participants addresses
    * @param token         the user token
    */
  sealed case class EnterPrivate(roomID: String, playerAddress: Address, webAddress: Address, token: String)

  /**
    * Enter a public room
    *
    * @param playersNumber the number of players the public room should have
    * @param playerAddress the player address that wants to join the room
    * @param webAddress    the address where the player wants to receive tha other participants addresses
    * @param token         the user token
    */
  sealed case class EnterPublic(playersNumber: Int, playerAddress: Address, webAddress: Address, token: String)

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