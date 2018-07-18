package it.cwmp.services.rooms

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.RoutingContext
import it.cwmp.controller.client.RoomReceiverApiWrapper
import it.cwmp.model.{Address, Room}
import it.cwmp.utils.{Loggable, VertxServer}

import scala.concurrent.{ExecutionContext, Future}

trait RoomsServiceUtils {
  this: VertxServer with Loggable =>

  private[rooms] val TOKEN_NOT_PROVIDED_OR_INVALID = "Token not provided or invalid"
  private[rooms] val INVALID_PARAMETER_ERROR = "Invalid parameters: "

  /**
    * A method to retrieve the database configuration
    *
    * @return the JsonObject representing database configuration
    */
  // TODO should be generalized in an exclusive trait
  private[rooms] def loadLocalDBConfig(vertx: Vertx)(implicit executionContext: ExecutionContext): Future[JsonObject] =
    vertx fileSystem() readFileFuture "database/jdbc_config.json" map (_.toJsonObject)

  /**
    * @param body the body where to extract
    * @return the extracted Addresses
    */
  private[rooms] def extractAddressesFromBody(body: Buffer): Option[(Address, Address)] = {
    try {
      val jsonArray = body.toJsonArray
      val userAddressJson = jsonArray.getJsonObject(0)
      val userNotificationAddressJson = jsonArray.getJsonObject(1)
      if ((userAddressJson containsKey Address.FIELD_ADDRESS) && (userNotificationAddressJson containsKey Address.FIELD_ADDRESS)) {
        import Address.Converters._
        Some((userAddressJson.toAddress, userNotificationAddressJson.toAddress))
      } else None
    } catch {
      case _: Throwable => None
    }
  }

  /**
    * Method that manages the filling of private rooms, fails if the room is not full
    */
  private[rooms] def handlePrivateRoomFilling(roomID: String)
                                      (implicit roomDAO: RoomDAO,
                                       routingContext: RoutingContext,
                                       communicationStrategy: RoomReceiverApiWrapper,
                                       executionContext: ExecutionContext): Future[Unit] = {
    handleRoomFilling(roomInformationFuture = roomDAO.roomInfo(roomID),
      onRetrievedRoomAction = roomDAO deleteRoom roomID map (_ => sendResponse(200)))
  }

  /**
    * Method that manages the filling of public rooms, fails if the room is not full
    */
  private[rooms] def handlePublicRoomFilling(playersNumber: Int)
                                     (implicit roomDAO: RoomDAO,
                                      routingContext: RoutingContext,
                                      communicationStrategy: RoomReceiverApiWrapper,
                                      executionContext: ExecutionContext): Future[Unit] = {
    handleRoomFilling(roomInformationFuture = roomDAO.publicRoomInfo(playersNumber),
      onRetrievedRoomAction = roomDAO deleteAndRecreatePublicRoom playersNumber map (_ => sendResponse(200)))
  }

  /**
    * Describes how to behave when a room is filled out
    *
    * @param roomInformationFuture the future that will contain the room and addresses of users to contact
    * @param onRetrievedRoomAction the action to do if the room is full
    * @return a future that completes when all players received addresses
    */
  private[this] def handleRoomFilling(roomInformationFuture: Future[(Room, Seq[Address])],
                                      onRetrievedRoomAction: => Future[Unit])
                                     (implicit communicationStrategy: RoomReceiverApiWrapper,
                                      executionContext: ExecutionContext): Future[Unit] = {
    roomInformationFuture.map(tuple => tuple._1).filter(roomIsFull)
      .flatMap(_ => {
        onRetrievedRoomAction
        sendParticipantAddresses(roomInformationFuture.value.get.get)
      })
  }

  /**
    * Describes when a room is full
    */
  private[rooms] def roomIsFull(room: Room): Boolean = room.participants.size == room.neededPlayersNumber

  /**
    * Method to communicate to clients that the game can start because of reaching the specified number of players
    *
    * @param roomInformation the room where players are waiting in
    */
  private[rooms] def sendParticipantAddresses(roomInformation: (Room, Seq[Address]))
                                      (implicit communicationStrategy: RoomReceiverApiWrapper,
                                       executionContext: ExecutionContext): Future[Unit] = {
    logger.info(s"Preparing to send participant list to room ${roomInformation._1.name} (with id:${roomInformation._1.identifier}) participants ...")
    val notificationAddresses = for (notificationAddress <- roomInformation._2) yield notificationAddress
    val players = for (player <- roomInformation._1.participants) yield player
    logger.info(s"Participant notification addresses to contact: $notificationAddresses")
    logger.info(s"Information to send -> $players")
    Future.sequence {
      notificationAddresses map (notificationAddress => communicationStrategy.sendParticipants(notificationAddress.address, players))
    } map (_ => Unit)
  }
}
