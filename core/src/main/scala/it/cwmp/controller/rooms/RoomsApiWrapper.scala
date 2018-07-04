package it.cwmp.controller.rooms

import it.cwmp.model.{Room, User}

import scala.concurrent.Future
import scala.language.implicitConversions

/**
  * A trait that describes the Api wrapper for Rooms
  *
  * @author Enrico Siboni
  */
trait RoomsApiWrapper {
  /**
    * @return the future that completes when initialization finishes
    */
  def initialize(): Future[Unit]

  /**
    * Creates a room
    *
    * @param roomName      the room name
    * @param playersNumber the players number
    * @return the future containing the identifier of the created room
    */
  def createRoom(roomName: String, playersNumber: Int): Future[String]

  /**
    * Enters a room
    *
    * @param roomID the identifier of the room
    * @param user   the user that wants to enter
    * @return the future that completes when the user has entered
    */
  def enterRoom(roomID: String)(implicit user: User): Future[Unit]

  /**
    * Retrieves room information
    *
    * @param roomID the identifier of the room
    * @return the future that completes when the room information is available
    */
  def roomInfo(roomID: String): Future[Room]

  /**
    * Exits a room
    *
    * @param roomID the identifier of the room to exit
    * @param user   the user that wants to exit
    * @return the future that completes when user has exited
    */
  def exitRoom(roomID: String)(implicit user: User): Future[Unit]

  /**
    * retrieves a list of available public rooms
    *
    * @return a future that completes when such list is available
    */
  def listPublicRooms(): Future[Seq[Room]]

  /**
    * Enters a public room
    *
    * @param playersNumber the number of players that the public room has to have
    * @param user          the user that wants to enter
    * @return the future that completes when user ha entered
    */
  def enterPublicRoom(playersNumber: Int)(implicit user: User): Future[Unit]

  /**
    * Retrieves information about a public room with specific number of players
    *
    * @param playersNumber the number of players that the public room has to have
    * @return the future that completes when the information is available
    */
  def publicRoomInfo(playersNumber: Int): Future[Room]

  /**
    * Exits a public room
    *
    * @param playersNumber the number of players that the public room has to have
    * @param user          the user that wants to exit
    * @return the future taht completes when the user has exited
    */
  def exitPublicRoom(playersNumber: Int)(implicit user: User): Future[Unit]
}

/**
  * Companion Object
  *
  * @author Enrico Siboni
  */
object RoomsApiWrapper {
  val publicPrefix: String = "public"
}