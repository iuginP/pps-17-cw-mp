package it.cwmp.services.wrapper

import io.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, NOT_FOUND}
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.{Address, Participant, Room, User}
import it.cwmp.utils.Utils
import it.cwmp.utils.Utils.httpStatusNameToCode

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Fake class implementing a version of the RoomsApiWrapper in memory.
  * This is useful when you need to test something that uses the rooms
  * and you don't want to start the complete service.
  */
case class FakeRoomsApiWrapper(authenticationApiWrapper: AuthenticationApiWrapper) extends RoomsApiWrapper {

  private val ROOM_ID_LENGTH = 10
  private val publicPrefix = "public"

  private var rooms: Seq[Room] = Seq(
    Room(publicPrefix + Utils.randomString(ROOM_ID_LENGTH), publicPrefix + 2, 2, Seq())
  )

  private var roomsParticipantAddresses: Map[String, Seq[String]] = Map(
    rooms.head.identifier -> Seq()
  )

  override def createRoom(roomName: String, playersNumber: Int)
                         (implicit userToken: String): Future[String] =
    Future {
      authenticatedUser(userToken)
      val roomID = Utils.randomString(ROOM_ID_LENGTH)
      rooms = rooms :+ Room(roomID, roomName, playersNumber, Seq())
      roomsParticipantAddresses = roomsParticipantAddresses + (roomID -> Seq())
      roomID
    }

  override def enterRoom(roomID: String, userAddress: Address, notificationAddress: Address)
                        (implicit userToken: String): Future[Unit] =
    Future {
      val user = authenticatedUser(userToken)
      val room = getRoomFromID(roomID)
      if (rooms.exists(room => room.participants.exists(_.username == user.username))) throw HTTPException(BAD_REQUEST)

      rooms = rooms.filterNot(_ == room) :+ Room(
        room.identifier,
        room.name,
        room.neededPlayersNumber,
        room.participants :+ Participant(user.username, userAddress.address))

      val newAddresses = roomsParticipantAddresses(roomID) :+ notificationAddress.address
      roomsParticipantAddresses = roomsParticipantAddresses - roomID + (roomID -> newAddresses)

      val afterEnteringRoom = getRoomFromID(roomID)
      if (afterEnteringRoom.participants.size == afterEnteringRoom.neededPlayersNumber) {
        // remove room if full
        rooms = rooms.filterNot(_ == afterEnteringRoom)
      }
    }

  override def roomInfo(roomID: String)
                       (implicit userToken: String): Future[Room] =
    Future {
      authenticatedUser(userToken)
      getRoomFromID(roomID)
    }

  override def exitRoom(roomID: String)
                       (implicit userToken: String): Future[Unit] =
    Future {
      val user = authenticatedUser(userToken)
      val room = getRoomFromID(roomID)
      if (!room.participants.exists(_.username == user.username)) throw HTTPException(BAD_REQUEST)
      if (room.participants.size == room.neededPlayersNumber) {
        throw HTTPException(BAD_REQUEST)
      } else {
        rooms = rooms.filterNot(_ == room) :+ Room(
          room.identifier,
          room.name,
          room.neededPlayersNumber,
          room.participants.filterNot(_.username == user.username)
        )
      }
    }

  override def listPublicRooms()(implicit userToken: String): Future[Seq[Room]] =
    Future {
      authenticatedUser(userToken)
      rooms.filter(_.identifier.startsWith(publicPrefix))
    }

  override def enterPublicRoom(playersNumber: Int, userAddress: Address, notificationAddress: Address)
                              (implicit userToken: String): Future[Unit] =
    enterRoom(getPublicRoomIDFromPlayersNumber(playersNumber), userAddress, notificationAddress)

  override def publicRoomInfo(playersNumber: Int)
                             (implicit userToken: String): Future[Room] =
    Future {
      roomInfo(getPublicRoomIDFromPlayersNumber(playersNumber)).value match {
        case Some(Success(room)) => room
        case Some(Failure(ex)) => throw ex
        case _ => throw mockedRoomsError
      }
    }

  override def exitPublicRoom(playersNumber: Int)
                             (implicit userToken: String): Future[Unit] =
    exitRoom(getPublicRoomIDFromPlayersNumber(playersNumber))

  /**
    * Handle method get user authenticated with provided token in this mocked version of ApiWrapper
    *
    * @param userToken the token to check
    */
  private def authenticatedUser(userToken: String): User =
    authenticationApiWrapper.validate(userToken).value match {
      case Some(Success(user)) => user
      case Some(Failure(ex)) => throw ex
      case _ => throw mockedRoomsError
    }

  /**
    * Handle method to get room in this mocked version of ApiWrapper
    *
    * @param roomID the roomId of room to retrieve
    * @return the room retrieved or throw exception if not found
    */
  private def getRoomFromID(roomID: String): Room = rooms.find(_.identifier == roomID) match {
    case Some(room) => room
    case _ => throw HTTPException(NOT_FOUND)
  }

  /**
    * Handle method to get roomID from room players number
    *
    * @param playersNumber the players number of the public room to retrieve ID
    * @return the roomID retrieved of exception if not found
    */
  private def getPublicRoomIDFromPlayersNumber(playersNumber: Int): String =
    rooms.find(room => room.identifier.startsWith(publicPrefix) && room.neededPlayersNumber == playersNumber) match {
      case Some(room) => room.identifier
      case _ => throw HTTPException(NOT_FOUND)
    }

  /**
    * @return error of the fake rooms api wrapper
    */
  private def mockedRoomsError: Throwable = new IllegalStateException("Something went wrong in mocked version of RoomsApiWrapper")
}
