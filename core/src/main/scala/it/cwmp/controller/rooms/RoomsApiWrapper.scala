package it.cwmp.controller.rooms

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod.{DELETE, GET, POST, PUT}
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.model.{Address, Room, User}

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
  def enterRoom(roomID: String)(implicit user: User with Address, userToken: String): Future[Unit]

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
    * @param user      the user that wants to exit
    * @param userToken the token to be authenticated against api
    * @return the future that completes when user has exited,
    *         or fails if roomId is not provided or not present
    *         or user is not inside that room
    */
  def exitRoom(roomID: String)(implicit user: User, userToken: String): Future[Unit]

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
  def enterPublicRoom(playersNumber: Int)(implicit user: User with Address, userToken: String): Future[Unit]

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
    * @param user          the user that wants to exit
    * @param userToken     the token to be authenticated against api
    * @return the future taht completes when the user has exited,
    *         or fails if players number is not correct,
    *         or user not inside that room
    */
  def exitPublicRoom(playersNumber: Int)(implicit user: User, userToken: String): Future[Unit]
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

  def apply()(implicit vertx: Vertx): RoomsApiWrapper = RoomsApiWrapper(DEFAULT_HOST, DEFAULT_PORT)

  def apply(host: String)(implicit vertx: Vertx): RoomsApiWrapper = RoomsApiWrapper(host, DEFAULT_PORT)

  def apply(host: String, port: Int)(implicit vertx: Vertx): RoomsApiWrapper = RoomsApiWrapperDefault(host, port)

  /**
    * Implementation of the Api Wrapper for rooms service
    *
    * @param host  the host with which this wrapper will communicate
    * @param port  the port on which this wrapper will communicate
    * @param vertx the vertx instance to use
    *
    */
  private case class RoomsApiWrapperDefault(host: String, port: Int)(implicit vertx: Vertx) extends RoomsApiWrapper {

    private implicit val executionContext: VertxExecutionContext = VertxExecutionContext(vertx.getOrCreateContext())
    private val client: WebClient = WebClient.create(vertx,
      WebClientOptions()
        .setDefaultHost(host)
        .setDefaultPort(port))

    override def createRoom(roomName: String, playersNumber: Int)(implicit userToken: String): Future[String] =
      _createPrivateRoom(roomName, playersNumber, userToken)
        .flatMap(response => response.statusCode() match {
          case 201 => Future.successful(response.bodyAsString().get)
          case httpCode => Future.failed(RoomsServiceException(httpCode, response.bodyAsString().get))
        })

    override def enterRoom(roomID: String)(implicit user: User with Address, userToken: String): Future[Unit] =
      _enterPrivateRoom(roomID, user.address, userToken)
        .flatMap(response => response.statusCode() match {
          case 200 => Future.successful(Unit)
          case httpCode => Future.failed(RoomsServiceException(httpCode, response.bodyAsString().get))
        })

    override def roomInfo(roomID: String)(implicit userToken: String): Future[Room] =
      _privateRoomInfo(roomID, userToken)
        .flatMap(response => response.statusCode() match {
          case 200 =>
            import Room.Converters._
            Future.successful(Json.fromObjectString(response.bodyAsString().get).toRoom)
          case httpCode => Future.failed(RoomsServiceException(httpCode, response.bodyAsString().get))
        })

    override def exitRoom(roomID: String)(implicit user: User, userToken: String): Future[Unit] =
      _exitPrivateRoom(roomID, userToken)
        .flatMap(response => response.statusCode() match {
          case 200 => Future.successful(Unit)
          case httpCode => Future.failed(RoomsServiceException(httpCode, response.bodyAsString().get))
        })

    override def listPublicRooms()(implicit userToken: String): Future[Seq[Room]] =
      _listPublicRooms(userToken)
        .flatMap(response => response.statusCode() match {
          case 200 =>
            import Room.Converters._
            Future.successful(Json.fromArrayString(response.bodyAsString().get).getList.toArray(Array[JsonObject]()).toSeq.map(_.toRoom))
          case httpCode => Future.failed(RoomsServiceException(httpCode, response.bodyAsString().get))
        })

    override def enterPublicRoom(playersNumber: Int)(implicit user: User with Address, userToken: String): Future[Unit] =
      _enterPublicRoom(playersNumber, user.address, userToken)
        .flatMap(response => response.statusCode() match {
          case 200 => Future.successful(Unit)
          case httpCode => Future.failed(RoomsServiceException(httpCode, response.bodyAsString().get))
        })

    override def publicRoomInfo(playersNumber: Int)(implicit userToken: String): Future[Room] =
      _publicRoomInfo(playersNumber, userToken)
        .flatMap(response => response.statusCode() match {
          case 200 =>
            import Room.Converters._
            Future.successful(Json.fromObjectString(response.bodyAsString().get).toRoom)
          case httpCode => Future.failed(RoomsServiceException(httpCode, response.bodyAsString().get))
        })

    override def exitPublicRoom(playersNumber: Int)(implicit user: User, userToken: String): Future[Unit] =
      _exitPublicRoom(playersNumber, userToken)
        .flatMap(response => response.statusCode() match {
          case 200 => Future.successful(Unit)
          case httpCode => Future.failed(RoomsServiceException(httpCode, response.bodyAsString().get))
        })

    // TODO: duplicated code from RoomsService test... ehat to do??
    // TODO: maybe i can use this wrapper where possible and do manual http call where needed in tests

    /**
      * Utility method to create a client request with certain parameters
      */
    private def createClientRequestWithToken(webClient: WebClient, httpMethod: HttpMethod, url: String, userToken: String) = {
      (httpMethod match {
        case GET => webClient.get(url)
        case POST => webClient.post(url)
        case PUT => webClient.put(url)
        case DELETE => webClient.delete(url)
      }).putHeader(HttpHeaderNames.AUTHORIZATION.toString, userToken)
    }

    private def _createPrivateRoom(roomName: String, neededPlayers: Int, userToken: String) = {
      createClientRequestWithToken(client, POST, RoomsApiWrapper.API_CREATE_PRIVATE_ROOM_URL, userToken)
        .sendJsonObjectFuture(roomForCreationJson(roomName, neededPlayers))
    }

    private def _enterPrivateRoom(roomID: String, userAddress: String, userToken: String) = {
      createClientRequestWithToken(client, PUT, RoomsApiWrapper.API_ENTER_PRIVATE_ROOM_URL, userToken)
        .setQueryParam(Room.FIELD_IDENTIFIER, roomID)
        .sendJsonObjectFuture(addressForEnteringJson(userAddress))
    }

    private def _privateRoomInfo(roomID: String, userToken: String) = {
      createClientRequestWithToken(client, GET, RoomsApiWrapper.API_PRIVATE_ROOM_INFO_URL, userToken)
        .setQueryParam(Room.FIELD_IDENTIFIER, roomID)
        .sendFuture()
    }

    private def _exitPrivateRoom(roomID: String, userToken: String) = {
      createClientRequestWithToken(client, DELETE, RoomsApiWrapper.API_EXIT_PRIVATE_ROOM_URL, userToken)
        .setQueryParam(Room.FIELD_IDENTIFIER, roomID)
        .sendFuture()
    }

    private def _listPublicRooms(userToken: String) = {
      createClientRequestWithToken(client, GET, RoomsApiWrapper.API_LIST_PUBLIC_ROOMS_URL, userToken)
        .sendFuture()
    }

    private def _enterPublicRoom(playersNumber: Int, userAddress: String, userToken: String) = {
      createClientRequestWithToken(client, PUT, RoomsApiWrapper.API_ENTER_PUBLIC_ROOM_URL, userToken)
        .setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString)
        .sendJsonObjectFuture(addressForEnteringJson(userAddress))
    }

    private def _publicRoomInfo(playersNumber: Int, userToken: String) = {
      createClientRequestWithToken(client, GET, RoomsApiWrapper.API_PUBLIC_ROOM_INFO_URL, userToken)
        .setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString)
        .sendFuture()
    }

    private def _exitPublicRoom(playersNumber: Int, userToken: String) = {
      createClientRequestWithToken(client, DELETE, RoomsApiWrapper.API_EXIT_PUBLIC_ROOM_URL, userToken)
        .setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString)
        .sendFuture()
    }

    /**
      * Handle method to create the JSON to use in creation API
      */
    private def roomForCreationJson(roomName: String, playersNumber: Int): JsonObject =
      Json.obj((Room.FIELD_NAME, roomName), (Room.FIELD_NEEDED_PLAYERS, playersNumber))

    /**
      * Handle method to create JSON to use in entering API
      */
    private def addressForEnteringJson(address: String): JsonObject =
      Json.obj((User.FIELD_ADDRESS, address))
  }

}