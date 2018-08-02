package it.cwmp.client.controller.messages

import it.cwmp.model.Address

/**
  * The collection of Rooms request messages
  */
object RoomsRequests {

  sealed trait RoomRequest extends Request


  sealed trait RoomPrivateRequest extends RoomRequest

  sealed trait RoomPublicRequest extends RoomRequest


  sealed trait RoomCreationRequest extends RoomRequest

  sealed trait RoomEnteringRequest extends RoomRequest

  sealed trait RoomExitingRequest extends RoomRequest

  /**
    * Create a new private room; request from GUI
    *
    * @param name          the room name
    * @param playersNumber the players number
    */
  sealed case class GUICreate(name: String, playersNumber: Int) extends RoomCreationRequest with GUIRequest

  /**
    * Enter a private room; request from GUI
    *
    * @param roomID the roomID to enter
    */
  sealed case class GUIEnterPrivate(roomID: String) extends RoomEnteringRequest with RoomPrivateRequest with GUIRequest

  /**
    * Enter a public room; request from GUI
    *
    * @param playersNumber the players number of the public room to enter
    */
  sealed case class GUIEnterPublic(playersNumber: Int) extends RoomEnteringRequest with RoomPublicRequest with GUIRequest

  /**
    * Exits a private room; request from GUI
    *
    * @param roomID the room id of room to exit
    */
  sealed case class GUIExitPrivate(roomID: String) extends RoomExitingRequest with RoomPrivateRequest with GUIRequest

  /**
    * Exits a public room; request from GUI
    *
    * @param playersNumber the players number of public room to exit
    */
  sealed case class GUIExitPublic(playersNumber: Int) extends RoomExitingRequest with RoomPublicRequest with GUIRequest

  /**
    * Create a new private room; request for online service
    *
    * @param name          the name of the room
    * @param playersNumber the players number
    * @param token         the user token
    */
  sealed case class ServiceCreate(name: String, playersNumber: Int, token: String) extends RoomCreationRequest with ToServiceRequest

  /**
    * Enter a private room; request for online service
    *
    * @param roomID        the room id
    * @param playerAddress the player address that wants to join the room
    * @param webAddress    the address where tha player wants to receive other participants addresses
    * @param token         the user token
    */
  sealed case class ServiceEnterPrivate(roomID: String, playerAddress: Address, webAddress: Address, token: String)
    extends RoomEnteringRequest with RoomPrivateRequest with ToServiceRequest

  /**
    * Enter a public room; request for online service
    *
    * @param playersNumber the number of players the public room should have
    * @param playerAddress the player address that wants to join the room
    * @param webAddress    the address where the player wants to receive tha other participants addresses
    * @param token         the user token
    */
  sealed case class ServiceEnterPublic(playersNumber: Int, playerAddress: Address, webAddress: Address, token: String)
    extends RoomEnteringRequest with RoomPublicRequest with ToServiceRequest

  /**
    * Exits a private room; request from GUI
    *
    * @param roomID the room id of room to exit
    * @param token  the user token
    */
  sealed case class ServiceExitPrivate(roomID: String, token: String) extends RoomExitingRequest with RoomPrivateRequest with ToServiceRequest

  /**
    * Exits a public room; request from GUI
    *
    * @param playersNumber the players number of public room to exit
    * @param token         the user token
    */
  sealed case class ServiceExitPublic(playersNumber: Int, token: String) extends RoomExitingRequest with RoomPublicRequest with ToServiceRequest

}

/**
  * The collection of Rooms response messages
  */
object RoomsResponses {

  sealed trait RoomResponse extends Response

  sealed trait RoomPrivateResponse extends RoomResponse

  sealed trait RoomPublicResponse extends RoomResponse


  sealed trait RoomCreationResponse extends RoomResponse

  sealed trait RoomEnteringResponse extends RoomResponse

  sealed trait RoomExitingResponse extends RoomResponse

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
  case object EnterPrivateSuccess extends RoomEnteringResponse with RoomPrivateResponse

  /**
    * Enter private room failed
    *
    * @param errorMessage optionally an error message
    */
  sealed case class EnterPrivateFailure(errorMessage: Option[String]) extends RoomEnteringResponse with RoomPrivateResponse

  /**
    * Enter public room succeeded
    */
  case object EnterPublicSuccess extends RoomEnteringResponse with RoomPublicResponse

  /**
    * Enter public room failed
    *
    * @param errorMessage optionally an error message
    */
  sealed case class EnterPublicFailure(errorMessage: Option[String]) extends RoomEnteringResponse with RoomPublicResponse

  /**
    * Exit private room succeeded
    */
  case object ExitPrivateSuccess extends RoomExitingResponse with RoomPrivateResponse

  /**
    * Exit private room failed
    *
    * @param errorMessage optionally an error message
    */
  sealed case class ExitPrivateFailure(errorMessage: Option[String]) extends RoomExitingResponse with RoomPrivateResponse

  /**
    * Exit public room succeeded
    */
  case object ExitPublicSuccess extends RoomExitingResponse with RoomPublicResponse

  /**
    * Exit public room failed
    *
    * @param errorMessage optionally an error message
    */
  sealed case class ExitPublicFailure(errorMessage: Option[String]) extends RoomExitingResponse with RoomPublicResponse

}
