package it.cwmp.controller.rooms

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod.{DELETE, GET, POST, PUT}
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.{HttpResponse, WebClient}
import it.cwmp.controller.ApiClient
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.{Address, Participant, Room, User}
import it.cwmp.model.Participant.Converters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

/**
  * A trait that describes the Api wrapper for Rooms
  *
  * @author Enrico Siboni
  */
trait RoomsApiWrapper {

  /**
    * Creates a room
    *
    * @param roomName      the room name
    * @param playersNumber the players number
    * @param userToken     the token to be authenticated against api
    * @return the future containing the identifier of the created room,
    *         or fails if roomName is empty or playersNumber not correct
    */
  def createRoom(roomName: String, playersNumber: Int)(implicit userToken: String): Future[String]

  /**
    * Enters a room
    *
    * @param roomID    the identifier of the room
    * @param user      the user that wants to enter
    * @param userToken the token to be authenticated against api
    * @return the future that completes when the user has entered,
    *         or fails if roomID not provided, not present
    *         or user already inside a room, or room full
    */
  def enterRoom(roomID: String)(implicit user: Participant, userToken: String): Future[Unit]

  /**
    * Retrieves room information
    *
    * @param roomID    the identifier of the room
    * @param userToken the token to be authenticated against api
    * @return the future that completes when the room information is available,
    *         or fails if room id not provided or not present
    */
  def roomInfo(roomID: String)(implicit userToken: String): Future[Room]

  /**
    * Exits a room
    *
    * @param roomID    the identifier of the room to exit
    * @param userToken the token to be authenticated against api
    * @return the future that completes when user has exited,
    *         or fails if roomId is not provided or not present
    *         or user is not inside that room
    */
  def exitRoom(roomID: String)(implicit userToken: String): Future[Unit]

  /**
    * retrieves a list of available public rooms
    *
    * @param userToken the token to be authenticated against api
    * @return a future that completes when such list is available
    */
  def listPublicRooms()(implicit userToken: String): Future[Seq[Room]]

  /**
    * Enters a public room
    *
    * @param playersNumber the number of players that the public room has to have
    * @param user          the user that wants to enter
    * @param userToken     the token to be authenticated against api
    * @return the future that completes when user ha entered,
    *         or fails if players number is not correct,
    *         or user already inside a room
    */
  def enterPublicRoom(playersNumber: Int)(implicit user: Participant, userToken: String): Future[Unit]

  /**
    * Retrieves information about a public room with specific number of players
    *
    * @param playersNumber the number of players that the public room has to have
    * @param userToken     the token to be authenticated against api
    * @return the future that completes when the information is available,
    *         or fails if players number not correct
    */
  def publicRoomInfo(playersNumber: Int)(implicit userToken: String): Future[Room]

  /**
    * Exits a public room
    *
    * @param playersNumber the number of players that the public room has to have
    * @param userToken     the token to be authenticated against api
    * @return the future taht completes when the user has exited,
    *         or fails if players number is not correct,
    *         or user not inside that room
    */
  def exitPublicRoom(playersNumber: Int)(implicit userToken: String): Future[Unit]
}

/**
  * Companion Object
  *
  * @author Enrico Siboni
  */
object RoomsApiWrapper {

  val API_CREATE_PRIVATE_ROOM_URL = "/api/rooms"
  val API_ENTER_PRIVATE_ROOM_URL = s"/api/rooms/:${Room.FIELD_IDENTIFIER}"
  val API_PRIVATE_ROOM_INFO_URL = s"/api/rooms/:${Room.FIELD_IDENTIFIER}"
  val API_EXIT_PRIVATE_ROOM_URL = s"/api/rooms/:${Room.FIELD_IDENTIFIER}/self"

  val API_LIST_PUBLIC_ROOMS_URL = "/api/rooms"
  val API_ENTER_PUBLIC_ROOM_URL = s"/api/rooms/public/:${Room.FIELD_NEEDED_PLAYERS}"
  val API_PUBLIC_ROOM_INFO_URL = s"/api/rooms/public/:${Room.FIELD_NEEDED_PLAYERS}"
  val API_EXIT_PUBLIC_ROOM_URL = s"/api/rooms/public/:${Room.FIELD_NEEDED_PLAYERS}/self"

  val DEFAULT_HOST = "localhost"
  val DEFAULT_PORT = 8667

  def apply(): RoomsApiWrapper = RoomsApiWrapper(DEFAULT_HOST, DEFAULT_PORT)

  def apply(host: String): RoomsApiWrapper = RoomsApiWrapper(host, DEFAULT_PORT)

  def apply(host: String, port: Int): RoomsApiWrapper = new RoomsApiWrapperDefault(host, port)

  /**
    * Implementation of the Api Wrapper for rooms service
    *
    * @param host the host with which this wrapper will communicate
    * @param port the port on which this wrapper will communicate
    *
    */
  private class RoomsApiWrapperDefault(host: String, port: Int) extends RoomsApiWrapper with ApiClient {

    private val vertx: Vertx = Vertx.vertx
    private implicit val executionContext: VertxExecutionContext = VertxExecutionContext(vertx.getOrCreateContext())

    private implicit val client: WebClient = createWebClient(host, port, vertx)

    override def createRoom(roomName: String, playersNumber: Int)(implicit userToken: String): Future[String] =
      RoomsApiWrapper.createPrivateRoom(roomName, playersNumber)
        .flatMap(implicit response => handleResponse(Future.successful(response.bodyAsString().get), 201))

    override def enterRoom(roomID: String)(implicit user: Participant, userToken: String): Future[Unit] =
      RoomsApiWrapper.enterPrivateRoom(roomID)
        .flatMap(implicit response => handleResponse(Future.successful(Unit), 200))

    override def roomInfo(roomID: String)(implicit userToken: String): Future[Room] =
      RoomsApiWrapper.privateRoomInfo(roomID)
        .flatMap(implicit response => handleResponse({
          import Room.Converters._
          Future.successful(Json.fromObjectString(response.bodyAsString().get).toRoom)
        }, 200))

    override def exitRoom(roomID: String)(implicit userToken: String): Future[Unit] =
      RoomsApiWrapper.exitPrivateRoom(roomID)
        .flatMap(implicit response => handleResponse(Future.successful(Unit), 200))

    override def listPublicRooms()(implicit userToken: String): Future[Seq[Room]] =
      RoomsApiWrapper.listPublicRooms()
        .flatMap(implicit response => handleResponse({
          Future.successful {
            import Room.Converters._
            Json.fromArrayString(response.bodyAsString().get)
              .stream().toArray().toSeq.map(_.toString)
              .map(jsonString => Json.fromObjectString(jsonString).toRoom)
          }
        }, 200))

    override def enterPublicRoom(playersNumber: Int)(implicit user: Participant, userToken: String): Future[Unit] =
      RoomsApiWrapper.enterPublicRoom(playersNumber)
        .flatMap(implicit response => handleResponse(Future.successful(Unit), 200))

    override def publicRoomInfo(playersNumber: Int)(implicit userToken: String): Future[Room] =
      RoomsApiWrapper.publicRoomInfo(playersNumber)
        .flatMap(implicit response => handleResponse({
          import Room.Converters._
          Future.successful(Json.fromObjectString(response.bodyAsString().get).toRoom)
        }, 200))

    override def exitPublicRoom(playersNumber: Int)(implicit userToken: String): Future[Unit] =
      RoomsApiWrapper.exitPublicRoom(playersNumber)
        .flatMap(implicit response => handleResponse(Future.successful(Unit), 200))

    /**
      * Utility method to handle the Service response
      */
    private def handleResponse[T](onSuccessFuture: => Future[T], successHttpCodes: Int*)(implicit response: HttpResponse[Buffer]) =
      successHttpCodes find (_ == response.statusCode) match {
        case Some(_) => onSuccessFuture
        case None => Future.failed(HTTPException(response.statusCode, response.bodyAsString))
      }
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService createPrivateRoom API
    */
  private[rooms] def createPrivateRoom(roomName: String, neededPlayers: Int)(implicit webClient: WebClient, userToken: String) = {
    createClientRequestWithToken(POST, API_CREATE_PRIVATE_ROOM_URL)(webClient, userToken)
      .flatMap(_.sendJsonObjectFuture(roomForCreationJson(roomName, neededPlayers)))
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService enterPrivateRoom API
    */
  private[rooms] def enterPrivateRoom(roomID: String)(implicit webClient: WebClient, participant: Participant, userToken: String) = {
    createClientRequestWithToken(PUT, API_ENTER_PRIVATE_ROOM_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_IDENTIFIER, roomID).sendJsonObjectFuture(participant.toJson))
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService privateRoomInfo API
    */
  private[rooms] def privateRoomInfo(roomID: String)(implicit webClient: WebClient, userToken: String) = {
    createClientRequestWithToken(GET, API_PRIVATE_ROOM_INFO_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_IDENTIFIER, roomID).sendFuture())
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService exitPrivateRoom API
    */
  private[rooms] def exitPrivateRoom(roomID: String)(implicit webClient: WebClient, userToken: String) = {
    createClientRequestWithToken(DELETE, API_EXIT_PRIVATE_ROOM_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_IDENTIFIER, roomID).sendFuture())
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService listPublicRooms API
    */
  private[rooms] def listPublicRooms()(implicit webClient: WebClient, userToken: String) = {
    createClientRequestWithToken(GET, API_LIST_PUBLIC_ROOMS_URL)(webClient, userToken)
      .flatMap(_.sendFuture())
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService enterPublicRoom API
    */
  private[rooms] def enterPublicRoom(playersNumber: Int)(implicit webClient: WebClient, participant: Participant, userToken: String) = {
    createClientRequestWithToken(PUT, API_ENTER_PUBLIC_ROOM_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString).sendJsonObjectFuture(participant.toJson))
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService publicRoomInfo API
    */
  private[rooms] def publicRoomInfo(playersNumber: Int)(implicit webClient: WebClient, userToken: String) = {
    createClientRequestWithToken(GET, API_PUBLIC_ROOM_INFO_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString).sendFuture())
  }

  /**
    * Wrapper method to do REST HTTP call to RoomsService exitPublicRoom API
    */
  private[rooms] def exitPublicRoom(playersNumber: Int)(implicit webClient: WebClient, userToken: String) = {
    createClientRequestWithToken(DELETE, API_EXIT_PUBLIC_ROOM_URL)(webClient, userToken)
      .flatMap(_.setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString).sendFuture())
  }

  /**
    * Utility method to build a client request with a token
    */
  private[rooms] def createClientRequestWithToken(httpMethod: HttpMethod, url: String)(implicit webClient: WebClient, userToken: String) =
    createClientRequest(webClient, httpMethod, url)
      .map(_.putHeader(HttpHeaderNames.AUTHORIZATION.toString, userToken))

  /**
    * Utility method to create a client request with certain parameters
    */
  private[rooms] def createClientRequest(webClient: WebClient, httpMethod: HttpMethod, url: String) =
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
  private[rooms] def roomForCreationJson(roomName: String, playersNumber: Int): JsonObject =
    Json.obj((Room.FIELD_NAME, roomName), (Room.FIELD_NEEDED_PLAYERS, playersNumber))

}