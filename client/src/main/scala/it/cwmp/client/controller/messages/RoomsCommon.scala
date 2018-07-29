package it.cwmp.client.controller.messages

import it.cwmp.model.Address

/**
  * The collection of Rooms request messages
  */
object RoomsRequests {

  sealed trait RoomRequest

  sealed trait RoomCreationRequest extends RoomRequest

  sealed trait RoomEnteringRequest extends RoomRequest

  sealed trait RoomPrivateEnteringRequest extends RoomEnteringRequest

  sealed trait RoomPublicEnteringRequest extends RoomEnteringRequest

  sealed trait RoomExitingRequest extends RoomRequest

  sealed trait RoomPrivateExitingRequest extends RoomExitingRequest

  sealed trait RoomPublicExitingRequest extends RoomExitingRequest

  /**
    * Create a new private room; request from GUI
    *
    * @param name          the room name
    * @param playersNumber the players number
    */
  sealed case class GUICreate(name: String, playersNumber: Int) extends RoomCreationRequest

  /**
    * Enter a private room; request from GUI
    *
    * @param roomID the roomID to enter
    */
  sealed case class GUIEnterPrivate(roomID: String) extends RoomPrivateEnteringRequest

  /**
    * Enter a public room; request from GUI
    *
    * @param playersNumber the players number of the public room to enter
    */
  sealed case class GUIEnterPublic(playersNumber: Int) extends RoomPublicEnteringRequest

  /**
    * Create a new private room; request for online service
    *
    * @param name          the name of the room
    * @param playersNumber the players number
    * @param token         the user token
    */
  sealed case class ServiceCreate(name: String, playersNumber: Int, token: String) extends RoomCreationRequest

  /**
    * Enter a private room; request for online service
    *
    * @param roomID        the room id
    * @param playerAddress the player address that wants to join the room
    * @param webAddress    the address where tha player wants to receive other participants addresses
    * @param token         the user token
    */
  sealed case class ServiceEnterPrivate(roomID: String, playerAddress: Address, webAddress: Address, token: String) extends RoomPrivateEnteringRequest

  /**
    * Enter a public room; request for online service
    *
    * @param playersNumber the number of players the public room should have
    * @param playerAddress the player address that wants to join the room
    * @param webAddress    the address where the player wants to receive tha other participants addresses
    * @param token         the user token
    */
  sealed case class ServiceEnterPublic(playersNumber: Int, playerAddress: Address, webAddress: Address, token: String) extends RoomPublicEnteringRequest


}

/**
  * The collection of Rooms response messages
  */
object RoomsResponses {

  sealed trait RoomCreationResponse

  sealed trait RoomEnteringResponse

  sealed trait RoomPrivateEnteringResponse extends RoomEnteringResponse

  sealed trait RoomPublicEnteringResponse extends RoomEnteringResponse

  /**
    * Creation successful
    *
    * @param roomID the room identifier to use entering the room
    */
  sealed case class CreateSuccess(roomID: String) extends RoomCreationResponse

  /**
    * Creation failed
    *
    * @param errorMessage optionally an error message
    */
  sealed case class CreateFailure(errorMessage: Option[String]) extends RoomCreationResponse

  /**
    * Enter private room succeeded
    */
  case object EnterPrivateSuccess extends RoomPrivateEnteringResponse

  /**
    * Enter private room failed
    *
    * @param errorMessage optionally an error message
    */
  sealed case class EnterPrivateFailure(errorMessage: Option[String]) extends RoomPrivateEnteringResponse

  /**
    * Enter public room succeeded
    */
  case object EnterPublicSuccess extends RoomPublicEnteringResponse

  /**
    * Enter public room failed
    *
    * @param errorMessage optionally an error message
    */
  sealed case class EnterPublicFailure(errorMessage: Option[String]) extends RoomPublicEnteringResponse

}