package it.cwmp.client.controller.messages

/**
  * The collection of Rooms response messages
  *
  * @author Enrico Siboni
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
