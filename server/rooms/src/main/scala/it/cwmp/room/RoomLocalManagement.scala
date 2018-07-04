package it.cwmp.room

import scala.concurrent.Future

/**
  * A trait that adds operations to manage local Rooms database
  *
  * @author Enrico Siboni
  */
trait RoomLocalManagement {
  def deleteRoom(roomID: String): Future[Unit]

  def deleteAndRecreatePublicRoom(playersNumber: Int): Future[Unit]
}
