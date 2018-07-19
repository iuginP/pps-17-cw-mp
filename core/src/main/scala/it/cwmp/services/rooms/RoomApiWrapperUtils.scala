package it.cwmp.services.rooms

import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.{Json, JsonArray, JsonObject}
import io.vertx.scala.ext.web.client.{HttpResponse, WebClient}
import it.cwmp.model.{Address, Room}
import it.cwmp.services.rooms.ServerParameters._
import it.cwmp.utils.VertxClient

import scala.concurrent.Future

trait RoomApiWrapperUtils {
  this: VertxClient =>

  /**
    * Wrapper method to do REST HTTP call to RoomsService createPrivateRoom API
    */
  protected def createPrivateRoomRequest(roomName: String, neededPlayers: Int)(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    client.post(API_CREATE_PRIVATE_ROOM_URL).addAuthentication.sendJsonObjectFuture(roomForCreationJson(roomName, neededPlayers))
  }

  /**
    * Handle method to create the JSON to use in creation API
    */
  protected def roomForCreationJson(roomName: String, playersNumber: Int): JsonObject =
    Json.obj((Room.FIELD_NAME, roomName), (Room.FIELD_NEEDED_PLAYERS, playersNumber))

  /**
    * Wrapper method to do REST HTTP call to RoomsService enterPrivateRoom API
    */
  protected def enterPrivateRoomRequest(roomID: String,
                                        userAddress: Address,
                                        notificationAddress: Address)
                                       (implicit webClient: WebClient,
                                        userToken: String): Future[HttpResponse[Buffer]] = {
    client.put(API_ENTER_PRIVATE_ROOM_URL).addAuthentication
      .setQueryParam(Room.FIELD_IDENTIFIER, roomID).sendJsonFuture(addressesForEnteringJson(userAddress, notificationAddress))
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService privateRoomInfo API
    */
  protected def privateRoomInfoRequest(roomID: String)(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    client.get(API_PRIVATE_ROOM_INFO_URL).addAuthentication
      .setQueryParam(Room.FIELD_IDENTIFIER, roomID).sendFuture()
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService exitPrivateRoom API
    */
  protected def exitPrivateRoomRequest(roomID: String)(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    client.delete(API_EXIT_PRIVATE_ROOM_URL).addAuthentication
      .setQueryParam(Room.FIELD_IDENTIFIER, roomID).sendFuture()
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService listPublicRooms API
    */
  protected def listPublicRoomsRequest()(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    client.get(API_LIST_PUBLIC_ROOMS_URL).addAuthentication
      .sendFuture()
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService enterPublicRoom API
    */
  protected def enterPublicRoomRequest(playersNumber: Int,
                                       userAddress: Address,
                                       notificationAddress: Address)
                                      (implicit webClient: WebClient,
                                       userToken: String): Future[HttpResponse[Buffer]] = {
    client.put(API_ENTER_PUBLIC_ROOM_URL).addAuthentication
      .setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString).sendJsonFuture(addressesForEnteringJson(userAddress, notificationAddress))
  }

  /**
    * Handle method to create the JSON to use in entering API
    */
  protected def addressesForEnteringJson(playerAddress: Address, notificationAddress: Address): JsonArray = {
    import Address.Converters._
    Json.arr(playerAddress.toJson, notificationAddress.toJson)
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService publicRoomInfo API
    */
  protected def publicRoomInfoRequest(playersNumber: Int)(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    client.get(API_PUBLIC_ROOM_INFO_URL).addAuthentication
      .setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString).sendFuture()
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService exitPublicRoom API
    */
  protected def exitPublicRoomRequest(playersNumber: Int)(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    client.delete(API_EXIT_PUBLIC_ROOM_URL).addAuthentication
      .setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString).sendFuture()
  }
}
