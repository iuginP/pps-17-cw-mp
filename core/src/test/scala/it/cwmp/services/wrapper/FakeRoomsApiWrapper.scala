package it.cwmp.services.wrapper

import io.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, NOT_FOUND, UNAUTHORIZED}
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
case class FakeRoomsApiWrapper() extends RoomsApiWrapper {

  val registeredUsers: Map[String, User] = Map(
    "TOKEN_1" -> User("Enrico"),
    "TOKEN_2" -> User("Eugenio"),
    "TOKEN_3" -> User("Elia"),
    "TOKEN_4" -> User("Davide")
  )

  private val ROOM_ID_LENGTH = 10
  private val publicPrefix = "public"

  private var rooms: Seq[Room] = Seq(
    Room(publicPrefix + Utils.randomString(ROOM_ID_LENGTH), publicPrefix + 2, 2, Seq())
  )
  private var roomsParticipantAddresses: Map[String, Seq[String]] = Map()

  override def createRoom(roomName: String, playersNumber: Int)
                         (implicit userToken: String): Future[String] =
    Future {
      checkAuthentication(userToken)
      val roomID = Utils.randomString(ROOM_ID_LENGTH)
      rooms = rooms :+ Room(roomID, roomName, playersNumber, Seq())
      roomsParticipantAddresses = roomsParticipantAddresses + (roomID -> Seq())
      roomID
    }

  override def enterRoom(roomID: String, userAddress: Address, notificationAddress: Address)
                        (implicit userToken: String): Future[Unit] =
    Future {
      checkAuthentication(userToken)
      val room = getRoomFromID(roomID)
      rooms = rooms.filterNot(_ == room) :+ Room(
        room.identifier,
        room.name,
        room.neededPlayersNumber,
        room.participants :+ Participant(registeredUsers(userToken).username, userAddress.address))

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
      checkAuthentication(userToken)
      getRoomFromID(roomID)
    }

  override def exitRoom(roomID: String)
                       (implicit userToken: String): Future[Unit] =
    Future {
      checkAuthentication(userToken)
      val room = getRoomFromID(roomID)
      if (room.participants.size == room.neededPlayersNumber) {
        throw HTTPException(BAD_REQUEST)
      } else {
        rooms = rooms.filterNot(_ == room) :+ Room(
          room.identifier,
          room.name,
          room.neededPlayersNumber,
          room.participants.filterNot(_.username == registeredUsers(userToken).username)
        )
      }
    }

  override def listPublicRooms()(implicit userToken: String): Future[Seq[Room]] =
    Future {
      checkAuthentication(userToken)
      rooms.filter(_.identifier.startsWith(publicPrefix))
    }

  override def enterPublicRoom(playersNumber: Int, userAddress: Address, notificationAddress: Address)
                              (implicit userToken: String): Future[Unit] =
    Future {
      checkAuthentication(userToken)
      enterRoom(getPublicRoomIDFromPlayersNumber(playersNumber), userAddress, notificationAddress)
    }

  override def publicRoomInfo(playersNumber: Int)
                             (implicit userToken: String): Future[Room] =
    Future {
      checkAuthentication(userToken)
      roomInfo(getPublicRoomIDFromPlayersNumber(playersNumber)).value match {
        case Some(Success(room)) => room
        case Some(Failure(ex)) => throw ex
        case _ => throw new IllegalStateException("Something went wrong in mocked version of RoomsApiWrapper")
      }
    }

  override def exitPublicRoom(playersNumber: Int)
                             (implicit userToken: String): Future[Unit] =
    Future {
      checkAuthentication(userToken)
      exitRoom(getPublicRoomIDFromPlayersNumber(playersNumber))
    }

  /**
    * Handle method to check authentication in this mocked version of ApiWrapper
    *
    * @param userToken the token to check
    */
  private def checkAuthentication(userToken: String): Unit = if (!registeredUsers.contains(userToken)) throw HTTPException(UNAUTHORIZED)

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
}
