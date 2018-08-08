package it.cwmp.services.wrapper

import it.cwmp.model.{Address, Room}

import scala.concurrent.Future

/**
  * Fake class implementing a version of the RoomsApiWrapper in memory.
  * This is useful when you need to test something that uses the rooms
  * and you don't want to start the complete service.
  */
case class FakeRoomsApiWrapper() extends RoomsApiWrapper {

  override def createRoom(roomName: String, playersNumber: Int)(implicit userToken: String): Future[String] = ???

  override def enterRoom(roomID: String, userAddress: Address, notificationAddress: Address)(implicit userToken: String): Future[Unit] = ???

  override def roomInfo(roomID: String)(implicit userToken: String): Future[Room] = ???

  override def exitRoom(roomID: String)(implicit userToken: String): Future[Unit] = ???

  override def listPublicRooms()(implicit userToken: String): Future[Seq[Room]] = ???

  override def enterPublicRoom(playersNumber: Int, userAddress: Address, notificationAddress: Address)(implicit userToken: String): Future[Unit] = ???

  override def publicRoomInfo(playersNumber: Int)(implicit userToken: String): Future[Room] = ???

  override def exitPublicRoom(playersNumber: Int)(implicit userToken: String): Future[Unit] = ???
}
