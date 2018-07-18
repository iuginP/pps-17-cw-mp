package it.cwmp.services.rooms

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod.{DELETE, GET, POST, PUT}
import io.vertx.lang.scala.json.{Json, JsonArray, JsonObject}
import io.vertx.scala.ext.web.client.{HttpRequest, HttpResponse, WebClient}
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.{Address, Room}
import it.cwmp.services.rooms.ServerParameters._
import it.cwmp.utils.HttpUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RoomApiWrapperUtils {


  /**
    * Wrapper method to do REST HTTP call to RoomsService createPrivateRoom API
    */
  protected def createPrivateRoomRequest(roomName: String, neededPlayers: Int)(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    createClientRequestWithToken(POST, API_CREATE_PRIVATE_ROOM_URL)(webClient, userToken)
      .flatMap(_.sendJsonObjectFuture(roomForCreationJson(roomName, neededPlayers)))
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService enterPrivateRoom API
    */
  protected def enterPrivateRoomRequest(roomID: String,
                                      userAddress: Address,
                                      notificationAddress: Address)
                                     (implicit webClient: WebClient,
                                      userToken: String): Future[HttpResponse[Buffer]] = {
    createClientRequestWithToken(PUT, API_ENTER_PRIVATE_ROOM_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_IDENTIFIER, roomID).sendJsonFuture(addressesForEnteringJson(userAddress, notificationAddress)))
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService privateRoomInfo API
    */
  protected def privateRoomInfoRequest(roomID: String)(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    createClientRequestWithToken(GET, API_PRIVATE_ROOM_INFO_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_IDENTIFIER, roomID).sendFuture())
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService exitPrivateRoom API
    */
  protected def exitPrivateRoomRequest(roomID: String)(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    createClientRequestWithToken(DELETE, API_EXIT_PRIVATE_ROOM_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_IDENTIFIER, roomID).sendFuture())
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService listPublicRooms API
    */
  protected def listPublicRoomsRequest()(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    createClientRequestWithToken(GET, API_LIST_PUBLIC_ROOMS_URL)(webClient, userToken)
      .flatMap(_.sendFuture())
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService enterPublicRoom API
    */
  protected def enterPublicRoomRequest(playersNumber: Int,
                                     userAddress: Address,
                                     notificationAddress: Address)
                                    (implicit webClient: WebClient,
                                     userToken: String): Future[HttpResponse[Buffer]] = {
    createClientRequestWithToken(PUT, API_ENTER_PUBLIC_ROOM_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString).sendJsonFuture(addressesForEnteringJson(userAddress, notificationAddress)))
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService publicRoomInfo API
    */
  protected def publicRoomInfoRequest(playersNumber: Int)(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    createClientRequestWithToken(GET, API_PUBLIC_ROOM_INFO_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString).sendFuture())
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService exitPublicRoom API
    */
  protected def exitPublicRoomRequest(playersNumber: Int)(implicit webClient: WebClient, userToken: String): Future[HttpResponse[Buffer]] = {
    createClientRequestWithToken(DELETE, API_EXIT_PUBLIC_ROOM_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString).sendFuture())
  }

  /**
    * Utility method to build a client request with a token
    */
  protected def createClientRequestWithToken(httpMethod: HttpMethod, url: String)(implicit webClient: WebClient, userToken: String): Future[HttpRequest[Buffer]] =
    HttpUtils.buildJwtAuthentication(userToken) match {
      case None => Future.failed(HTTPException(400, Some("Missing authorization token")))
      case Some(token) => createClientRequest(webClient, httpMethod, url)
        .map(_.putHeader(HttpHeaderNames.AUTHORIZATION.toString, token))
    }

  /**
    * Utility method to create a client request with certain parameters
    */
  protected def createClientRequest(webClient: WebClient, httpMethod: HttpMethod, url: String) =
    Future {
      httpMethod match {
        case GET => webClient.get(url)
        case POST => webClient.post(url)
        case PUT => webClient.put(url)
        case DELETE => webClient.delete(url)
        case _ => throw new IllegalStateException("Unrecognized HTTP method")
      }
    }

  /**
    * Handle method to create the JSON to use in creation API
    */
  protected def roomForCreationJson(roomName: String, playersNumber: Int): JsonObject =
    Json.obj((Room.FIELD_NAME, roomName), (Room.FIELD_NEEDED_PLAYERS, playersNumber))

  /**
    * Handle method to create the JSON to use in entering API
    */
  protected def addressesForEnteringJson(playerAddress: Address, notificationAddress: Address): JsonArray = {
    import Address.Converters._
    Json.arr(playerAddress.toJson, notificationAddress.toJson)
  }
}
