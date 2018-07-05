package it.cwmp.room

import scala.concurrent.Future

/**
  * A trait that adds operations to manage local Rooms database
  *
  * @author Enrico Siboni
  */
trait RoomLocalManagement {

  /**
    * Deletes a room if it's full
    *
    * @param roomID the id of the room to delete
    * @return the future that completes when deletion done,
    *         or fails if roomId is not provided or not present,
    *         of room is not full
    */
  def deleteRoom(roomID: String): Future[Unit]

  /**
    * Deletes a public room when it's full and recreates a new public room with same players number
    *
    * @param playersNumber the number of players that the public room has to have
    * @return the future that completes when the job is done,
    *         or fails if players number not correct,
    *         or that room is not full
    */
  def deleteAndRecreatePublicRoom(playersNumber: Int): Future[Unit]
}
