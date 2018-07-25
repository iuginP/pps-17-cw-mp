package it.cwmp.services.wrapper

import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.client.{HttpResponse, WebClientOptions}
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.Room.Converters._
import it.cwmp.model.{Address, Room}
import it.cwmp.services.rooms.RoomApiWrapperUtils
import it.cwmp.services.rooms.ServerParameters._
import it.cwmp.utils.{VertxClient, VertxInstance}

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
  def createRoom(roomName: String, playersNumber: Int)
                (implicit userToken: String): Future[String]

  /**
    * Enters a room
    *
    * @param roomID              the identifier of the room
    * @param userAddress         the address of the paler that wants to enter
    * @param notificationAddress the address where player wants to receive notification of room filling
    * @param userToken           the token to be authenticated against api
    * @return the future that completes when the user has entered,
    *         or fails if roomID not provided, not present
    *         or user already inside a room, or room full
    */
  def enterRoom(roomID: String, userAddress: Address, notificationAddress: Address)
               (implicit userToken: String): Future[Unit]

  /**
    * Retrieves room information
    *
    * @param roomID    the identifier of the room
    * @param userToken the token to be authenticated against api
    * @return the future that completes when the room information is available,
    *         or fails if room id not provided or not present
    */
  def roomInfo(roomID: String)
              (implicit userToken: String): Future[Room]

  /**
    * Exits a room
    *
    * @param roomID    the identifier of the room to exit
    * @param userToken the token to be authenticated against api
    * @return the future that completes when user has exited,
    *         or fails if roomId is not provided or not present
    *         or user is not inside that room
    */
  def exitRoom(roomID: String)
              (implicit userToken: String): Future[Unit]

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
    * @param playersNumber       the number of players that the public room has to have
    * @param userAddress         the address of the paler that wants to enter
    * @param notificationAddress the address where player wants to receive notification of room filling
    * @param userToken           the token to be authenticated against api
    * @return the future that completes when user ha entered,
    *         or fails if players number is not correct,
    *         or user already inside a room
    */
  def enterPublicRoom(playersNumber: Int, userAddress: Address, notificationAddress: Address)
                     (implicit userToken: String): Future[Unit]

  /**
    * Retrieves information about a public room with specific number of players
    *
    * @param playersNumber the number of players that the public room has to have
    * @param userToken     the token to be authenticated against api
    * @return the future that completes when the information is available,
    *         or fails if players number not correct
    */
  def publicRoomInfo(playersNumber: Int)
                    (implicit userToken: String): Future[Room]

  /**
    * Exits a public room
    *
    * @param playersNumber the number of players that the public room has to have
    * @param userToken     the token to be authenticated against api
    * @return the future taht completes when the user has exited,
    *         or fails if players number is not correct,
    *         or user not inside that room
    */
  def exitPublicRoom(playersNumber: Int)
                    (implicit userToken: String): Future[Unit]
}

/**
  * Companion Object
  *
  * @author Enrico Siboni
  */
object RoomsApiWrapper {

  val DEFAULT_HOST = "localhost"

  def apply(): RoomsApiWrapper = RoomsApiWrapper(DEFAULT_HOST, DEFAULT_PORT)

  def apply(host: String): RoomsApiWrapper = RoomsApiWrapper(host, DEFAULT_PORT)

  def apply(host: String, port: Int): RoomsApiWrapper = new RoomsApiWrapperDefault(WebClientOptions()
    .setDefaultHost(host)
    .setDefaultPort(port))

  /**
    * Implementation of the Api Wrapper for rooms service
    *
    * @param clientOptions the implicit configuration for the client connection to the host
    *
    */
  private class RoomsApiWrapperDefault(override protected val clientOptions: WebClientOptions)
    extends RoomsApiWrapper with RoomApiWrapperUtils with VertxInstance with VertxClient {

    override def createRoom(roomName: String, playersNumber: Int)
                           (implicit userToken: String): Future[String] =
      createPrivateRoomRequest(roomName, playersNumber)
        .flatMap(implicit response => handleResponse(Future.successful(response.bodyAsString().get), 201))

    override def enterRoom(roomID: String, userAddress: Address, notificationAddress: Address)
                          (implicit userToken: String): Future[Unit] =
      enterPrivateRoomRequest(roomID, userAddress, notificationAddress)
        .flatMap(implicit response => handleResponse(Future.successful(Unit), 200))

    override def roomInfo(roomID: String)
                         (implicit userToken: String): Future[Room] =
      privateRoomInfoRequest(roomID)
        .flatMap(implicit response => handleResponse({
          Future.successful(Json.fromObjectString(response.bodyAsString().get).toRoom)
        }, 200))

    override def exitRoom(roomID: String)
                         (implicit userToken: String): Future[Unit] =
      exitPrivateRoomRequest(roomID)
        .flatMap(implicit response => handleResponse(Future.successful(Unit), 200))

    override def listPublicRooms()(implicit userToken: String): Future[Seq[Room]] =
      listPublicRoomsRequest()
        .flatMap(implicit response => handleResponse({
          Future.successful {
            Json.fromArrayString(response.bodyAsString().get)
              .stream().toArray().toSeq.map(_.toString)
              .map(jsonString => Json.fromObjectString(jsonString).toRoom)
          }
        }, 200))

    override def enterPublicRoom(playersNumber: Int, userAddress: Address, notificationAddress: Address)
                                (implicit userToken: String): Future[Unit] =
      enterPublicRoomRequest(playersNumber, userAddress, notificationAddress)
        .flatMap(implicit response => handleResponse(Future.successful(Unit), 200))

    override def publicRoomInfo(playersNumber: Int)
                               (implicit userToken: String): Future[Room] =
      publicRoomInfoRequest(playersNumber)
        .flatMap(implicit response => handleResponse({
          Future.successful(Json.fromObjectString(response.bodyAsString().get).toRoom)
        }, 200))

    override def exitPublicRoom(playersNumber: Int)
                               (implicit userToken: String): Future[Unit] =
      exitPublicRoomRequest(playersNumber)
        .flatMap(implicit response => handleResponse(Future.successful(Unit), 200))

    /**
      * Utility method to handle the Service response
      */
    private def handleResponse[T](onSuccessFuture: => Future[T], successHttpCodes: Int*)
                                 (implicit response: HttpResponse[Buffer]) =
      successHttpCodes find (_ == response.statusCode) match {
        case Some(_) => onSuccessFuture
        case None => response.bodyAsString match {
          case Some(body) =>
            Future.failed(HTTPException(response.statusCode, body))
          case None =>
            Future.failed(HTTPException(response.statusCode))
        }
      }
  }

}