package it.cwmp.controller.rooms

import com.typesafe.scalalogging.Logger
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.json.Json
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.controller.client.RoomReceiverApiWrapper
import it.cwmp.controller.rooms.RoomsServiceVerticle._
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.{Address, Participant, Room, User}
import it.cwmp.utils.{Validation, VertxServer}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Class that implements the Rooms micro-service
  *
  * @author Enrico Siboni
  */
case class RoomsServiceVerticle(validationStrategy: Validation[String, User],
                                implicit val clientCommunicationStrategy: RoomReceiverApiWrapper) extends VertxServer {

  private var daoFuture: Future[RoomDAO] = _

  override protected val serverPort: Int = RoomsApiWrapper.DEFAULT_PORT

  override protected def initRouter(router: Router): Unit = {
    import RoomsApiWrapper._
    router post API_CREATE_PRIVATE_ROOM_URL handler createPrivateRoomHandler
    router put API_ENTER_PRIVATE_ROOM_URL handler enterPrivateRoomHandler
    router get API_PRIVATE_ROOM_INFO_URL handler privateRoomInfoHandler
    router delete API_EXIT_PRIVATE_ROOM_URL handler exitPrivateRoomHandler

    router get API_LIST_PUBLIC_ROOMS_URL handler listPublicRoomsHandler
    router put API_ENTER_PUBLIC_ROOM_URL handler enterPublicRoomHandler
    router get API_PUBLIC_ROOM_INFO_URL handler publicRoomInfoHandler
    router delete API_EXIT_PUBLIC_ROOM_URL handler exitPublicRoomHandler
  }

  override protected def initServer: Future[_] = {
    daoFuture = loadLocalDBConfig(vertx)
      .map(JDBCClient.createShared(vertx, _))
      .map(RoomsLocalDAO(_))
      .flatMap(dao => dao.initialize() map (_ => dao))
    daoFuture
  }

  /**
    * Handles the creation of a private room
    */
  private def createPrivateRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    logger.info("Create Private Room Request received...")
    routingContext.request().bodyHandler(body =>
      validateUserOrSendError.map(_ =>
        extractIncomingRoomFromBody(body) match {
          case Some((roomName, neededPlayers)) =>
            daoFuture.map(_.createRoom(roomName, neededPlayers)
              .onComplete {
                case Success(generatedID) => sendResponse(201, Some(generatedID))
                case Failure(ex) => sendResponse(400, Some(ex.getMessage))
              })
          case None =>
            logger.warn("Request body for room creation is required!")
            sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR no Room JSON in body"))
        }))

    def extractIncomingRoomFromBody(body: Buffer): Option[(String, Int)] = {
      try {
        val jsonObject = body.toJsonObject
        if ((jsonObject containsKey Room.FIELD_NAME) && (jsonObject containsKey Room.FIELD_NEEDED_PLAYERS))
          Some((jsonObject getString Room.FIELD_NAME, jsonObject getInteger Room.FIELD_NEEDED_PLAYERS))
        else None
      } catch {
        case _: Throwable => None
      }
    }
  }

  /**
    * Handles entering in a private room
    */
  private def enterPrivateRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    logger.info("Enter Private Room Request received...")
    routingContext.request().bodyHandler(body =>
      validateUserOrSendError.map(user => {
        (extractRequestParam(Room.FIELD_IDENTIFIER), extractAddressesFromBody(body)) match {
          case (Some(roomID), Some(addresses)) =>
            daoFuture.map(implicit dao => dao.enterRoom(roomID)(Participant(user.username, addresses._1.address), addresses._2).onComplete {
              case Success(_) => handlePrivateRoomFilling(roomID)
                .transform({
                  case Success(_) => Failure(new Exception())
                  case Failure(_: NoSuchElementException) => Success(Unit)
                  case Failure(ex) => logger.error("Error handling Private Room Filling", ex); Success(Unit)
                })
                .map(_ => sendResponse(200, None))
              case Failure(ex: NoSuchElementException) => sendResponse(404, Some(ex.getMessage))
              case Failure(ex) => sendResponse(400, Some(ex.getMessage))
            })

          case _ =>
            logger.warn("Player and notification address are required for entering a room!")
            sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR ${Room.FIELD_IDENTIFIER} or ${Address.FIELD_ADDRESS}"))
        }
      }))
  }

  /**
    * Handles retrieving info of a private room
    */
  private def privateRoomInfoHandler: Handler[RoutingContext] = implicit routingContext => {
    logger.info("Private Room Info Request received...")
    validateUserOrSendError.map(_ => {
      extractRequestParam(Room.FIELD_IDENTIFIER) match {
        case Some(roomId) =>
          daoFuture.map(_.roomInfo(roomId).map(_._1).onComplete {
            case Success(room) =>
              import Room.Converters._
              sendResponse(200, Some(room.toJson.encode()))
            case Failure(ex: NoSuchElementException) => sendResponse(404, Some(ex.getMessage))
            case Failure(ex) => sendResponse(400, Some(ex.getMessage))
          })
        case None => sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR ${Room.FIELD_IDENTIFIER}"))
      }
    })
  }

  /**
    * Handles exititng a private room
    */
  private def exitPrivateRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    logger.info("Exit Private Room Request received...")
    validateUserOrSendError.map(implicit user => {
      extractRequestParam(Room.FIELD_IDENTIFIER) match {
        case Some(roomId) =>
          daoFuture.map(_.exitRoom(roomId).onComplete {
            case Success(_) => sendResponse(200, None)
            case Failure(ex: NoSuchElementException) => sendResponse(404, Some(ex.getMessage))
            case Failure(ex) => sendResponse(400, Some(ex.getMessage))
          })
        case None => sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR ${Room.FIELD_IDENTIFIER}"))
      }
    })
  }

  /**
    * Handles listing of public rooms
    */
  private def listPublicRoomsHandler: Handler[RoutingContext] = implicit routingContext => {
    logger.info("List Public Rooms Request received...")
    validateUserOrSendError.map(_ => {
      daoFuture.map(_.listPublicRooms().onComplete {
        case Success(rooms) =>
          import Room.Converters._
          val jsonArray = rooms.foldLeft(Json emptyArr())(_ add _.toJson)
          sendResponse(200, Some(jsonArray encode()))
        case Failure(ex) => sendResponse(400, Some(ex.getMessage))
      })
    })
  }

  /**
    * Handles entering in a public room
    */
  private def enterPublicRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    logger.info("Enter Public Room Request received...")
    routingContext.request().bodyHandler(body =>
      validateUserOrSendError.map(user => {
        val playersNumberOption = try extractRequestParam(Room.FIELD_NEEDED_PLAYERS).map(_.toInt)
        catch {
          case _: Throwable => None
        }
        (playersNumberOption, extractAddressesFromBody(body)) match {
          case (Some(playersNumber), Some(addresses)) =>
            daoFuture.map(implicit dao => dao.enterPublicRoom(playersNumber)(Participant(user.username, addresses._1.address), addresses._2).onComplete {
              case Success(_) => handlePublicRoomFilling(playersNumber)
                .transform({
                  case Success(_) => Failure(new Exception())
                  case Failure(_: NoSuchElementException) => Success(Unit)
                  case Failure(ex) => logger.error("Error handling Public Room Filling", ex); Success(Unit)
                })
                .map(_ => sendResponse(200, None))
              case Failure(ex: NoSuchElementException) => sendResponse(404, Some(ex.getMessage))
              case Failure(ex) => sendResponse(400, Some(ex.getMessage))
            })

          case _ =>
            logger.warn("The number of players in url and player/notification address in body are required for public room entering")
            sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR ${Room.FIELD_NEEDED_PLAYERS} or ${Address.FIELD_ADDRESS}"))
        }
      }))
  }

  /**
    * Handles retrieving info of a public room
    */
  private def publicRoomInfoHandler: Handler[RoutingContext] = implicit routingContext => {
    logger.info("Public Room Info Request received...")
    validateUserOrSendError.map(_ => {
      (try extractRequestParam(Room.FIELD_NEEDED_PLAYERS).map(_.toInt)
      catch {
        case _: Throwable => None
      }) match {
        case Some(playersNumber) =>
          daoFuture.map(_.publicRoomInfo(playersNumber).map(_._1).onComplete {
            case Success(room) =>
              import Room.Converters._
              sendResponse(200, Some(room.toJson.encode()))
            case Failure(ex: NoSuchElementException) => sendResponse(404, Some(ex.getMessage))
            case Failure(ex) => sendResponse(400, Some(ex.getMessage))
          })
        case None =>
          logger.warn("The number of players in url is required for public room info retrieval")
          sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR ${Room.FIELD_NEEDED_PLAYERS}"))
      }
    })
  }

  /**
    * Handles exiting a public room
    */
  private def exitPublicRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    logger.info("Exit Public Room Request received...")
    validateUserOrSendError.map(implicit user => {
      (try extractRequestParam(Room.FIELD_NEEDED_PLAYERS).map(_.toInt)
      catch {
        case _: Throwable => None
      }) match {
        case Some(roomId) =>
          daoFuture.map(_.exitPublicRoom(roomId).onComplete {
            case Success(_) => sendResponse(200, None)
            case Failure(ex: NoSuchElementException) => sendResponse(404, Some(ex.getMessage))
            case Failure(ex) => sendResponse(400, Some(ex.getMessage))
          })
        case None =>
          logger.warn("The number of players in url is required for public room exiting")
          sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR ${Room.FIELD_NEEDED_PLAYERS}"))
      }
    })
  }


  /**
    * Checks whether the user is authenticated;
    * if token not provided sends back 400
    * if token invalid sends back 401
    *
    * @param routingContext the context in which to check
    * @return the future that will contain the user if successfully validated
    */
  private def validateUserOrSendError(implicit routingContext: RoutingContext): Future[User] = {
    routingContext.request().getAuthentication match {
      case None =>
        logger.warn("No authorization header in request")
        sendResponse(400, Some(TOKEN_NOT_PROVIDED_OR_INVALID))
        Future.failed(new IllegalAccessException(TOKEN_NOT_PROVIDED_OR_INVALID))

      case Some(authorizationToken) =>
        validationStrategy.validate(authorizationToken)
          .recoverWith {
            case HTTPException(statusCode, errorMessage) =>
              sendResponse(statusCode, errorMessage)
              Future.failed(new IllegalAccessException(errorMessage.getOrElse("")))
          } andThen {
          case Success(user) => logger.info(s"Validation succeeded: $user")
          case Failure(ex) => logger.error("Error validating user", ex)
        }
    }
  }
}

/**
  * Companion object
  *
  * @author Enrico Siboni
  */
object RoomsServiceVerticle {

  private val logger: Logger = Logger[RoomsServiceVerticle]

  private val TOKEN_NOT_PROVIDED_OR_INVALID = "Token not provided or invalid"
  private val INVALID_PARAMETER_ERROR = "Invalid parameters: "

  /**
    * A method to retrieve the database configuration
    *
    * @return the JsonObject representing database configuration
    */
  private def loadLocalDBConfig(vertx: Vertx)(implicit executionContext: ExecutionContext): Future[JsonObject] =
    vertx fileSystem() readFileFuture "database/jdbc_config.json" map (_.toJsonObject)

  /**
    * @param routingContext the routing context on which to extract
    * @return the extracted room name
    */
  private def extractRequestParam(paramName: String)(implicit routingContext: RoutingContext): Option[String] = {
    routingContext.request().getParam(paramName)
  }

  /**
    * @param body the body where to extract
    * @return the extracted Addresses
    */
  private def extractAddressesFromBody(body: Buffer): Option[(Address, Address)] = {
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
  private def handlePrivateRoomFilling(roomID: String)
                                      (implicit roomDAO: RoomDAO,
                                       routingContext: RoutingContext,
                                       communicationStrategy: RoomReceiverApiWrapper,
                                       executionContext: ExecutionContext): Future[Unit] = {
    handleRoomFilling(roomInformationFuture = roomDAO.roomInfo(roomID),
      onRetrievedRoomAction = roomDAO deleteRoom roomID map (_ => sendResponse(200, None)))
  }

  /**
    * Method that manages the filling of public rooms, fails if the room is not full
    */
  private def handlePublicRoomFilling(playersNumber: Int)
                                     (implicit roomDAO: RoomDAO,
                                      routingContext: RoutingContext,
                                      communicationStrategy: RoomReceiverApiWrapper,
                                      executionContext: ExecutionContext): Future[Unit] = {
    handleRoomFilling(roomInformationFuture = roomDAO.publicRoomInfo(playersNumber),
      onRetrievedRoomAction = roomDAO deleteAndRecreatePublicRoom playersNumber map (_ => sendResponse(200, None)))
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
  private def roomIsFull(room: Room): Boolean = room.participants.size == room.neededPlayersNumber

  /**
    * Method to communicate to clients that the game can start because of reaching the specified number of players
    *
    * @param roomInformation the room where players are waiting in
    */
  private def sendParticipantAddresses(roomInformation: (Room, Seq[Address]))
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

  /**
    * Utility method to send back responses
    *
    * @param routingContext the routing context in wich to send the error
    * @param httpCode       the http code
    * @param message        the message to send back
    */
  private def sendResponse(httpCode: Int,
                           message: Option[String])
                          (implicit routingContext: RoutingContext): Unit = {
    val response = routingContext.response().setStatusCode(httpCode)
    logger.info(s"Sending $httpCode response to client with message: $message")
    message match {
      case Some(messageString) => response.end(messageString)
      case None => response.end()
    }
  }
}