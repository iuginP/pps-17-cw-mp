package it.cwmp.services.rooms

import io.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, CREATED, NOT_FOUND, OK}
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.model.Address.Converters._
import it.cwmp.model.Room.Converters._
import it.cwmp.model.{Address, Participant, Room, User}
import it.cwmp.services.rooms.RoomsServiceVerticle.{INVALID_PARAMETER_ERROR, _}
import it.cwmp.services.rooms.ServerParameters._
import it.cwmp.services.wrapper.RoomReceiverApiWrapper
import it.cwmp.utils.Utils.{httpStatusNameToCode, stringToOption}
import it.cwmp.utils.{Logging, Validation, VertxServer}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Class that implements the Rooms micro-service
  *
  * @author Enrico Siboni
  */
class RoomsServiceVerticle(implicit val validationStrategy: Validation[String, User],
                           implicit val clientCommunicationStrategy: RoomReceiverApiWrapper)
  extends VertxServer with Logging {

  private var daoFuture: Future[RoomDAO] = _

  override protected val serverPort: Int = DEFAULT_PORT

  override protected def initRouter(router: Router): Unit = {
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
    val roomDAO = RoomsLocalDAO()
    daoFuture = roomDAO.initialize().map(_ => roomDAO)
    daoFuture
  }

  /**
    * Handles the creation of a private room
    */
  private def createPrivateRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Create Private Room Request received...")
    request.bodyHandler(body =>
      request.checkAuthenticationOrReject.map(_ =>
        extractIncomingRoomFromBody(body) match {
          case Some((roomName, neededPlayers)) =>
            daoFuture.map(_.createRoom(roomName, neededPlayers).onComplete {
              failureHandler orElse successHandler(generatedID => sendResponse(CREATED, generatedID))
            })
          case None =>
            log.warn("Request body for room creation is required!")
            sendResponse(BAD_REQUEST, s"$INVALID_PARAMETER_ERROR no Room JSON in body")
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
    log.info("Enter Private Room Request received...")
    request.bodyHandler(body =>
      request.checkAuthenticationOrReject.map(user => {
        (getRequestParameter(Room.FIELD_IDENTIFIER), extractAddressesFromBody(body)) match {
          case (Some(roomID), Some(addresses)) =>
            daoFuture.map(implicit dao => dao.enterRoom(roomID)(Participant(user.username, addresses._1.address), addresses._2).onComplete {
              failureHandler orElse successHandler(_ => handlePrivateRoomFilling(roomID) map (_ => sendResponse(OK)))
            })

          case _ =>
            log.warn("Player and notification address are required for entering a room!")
            sendResponse(BAD_REQUEST, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_IDENTIFIER} or ${Address.FIELD_ADDRESS}")
        }
      }))
  }

  /**
    * Handles retrieving info of a private room
    */
  private def privateRoomInfoHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Private Room Info Request received...")
    request.checkAuthenticationOrReject.map(_ => {
      getRequestParameter(Room.FIELD_IDENTIFIER) match {
        case Some(roomId) =>
          daoFuture.map(_.roomInfo(roomId).map(_._1).onComplete {
            failureHandler orElse successHandler(room => sendResponse(OK, room.toJson.encode()))
          })
        case None => sendResponse(BAD_REQUEST, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_IDENTIFIER}")
      }
    })
  }

  /**
    * Handles exititng a private room
    */
  private def exitPrivateRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Exit Private Room Request received...")
    request.checkAuthenticationOrReject.map(implicit user => {
      getRequestParameter(Room.FIELD_IDENTIFIER) match {
        case Some(roomId) =>
          daoFuture.map(_.exitRoom(roomId).onComplete {
            failureHandler orElse successHandler(_ => sendResponse(OK))
          })
        case None => sendResponse(BAD_REQUEST, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_IDENTIFIER}")
      }
    })
  }

  /**
    * Handles listing of public rooms
    */
  private def listPublicRoomsHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("List Public Rooms Request received...")
    request.checkAuthenticationOrReject.map(_ =>
      daoFuture.map(_.listPublicRooms().onComplete {
        failureHandler orElse
          successHandler(rooms => sendResponse(OK, rooms.foldLeft(Json emptyArr())(_ add _.toJson) encode()))
      }))
  }

  /**
    * Handles entering in a public room
    */
  private def enterPublicRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Enter Public Room Request received...")
    request.bodyHandler(body =>
      request.checkAuthenticationOrReject.map(user => {
        val playersNumberOption = try getRequestParameter(Room.FIELD_NEEDED_PLAYERS).map(_.toInt)
        catch {
          case _: Throwable => None
        }
        (playersNumberOption, extractAddressesFromBody(body)) match {
          case (Some(playersNumber), Some(addresses)) =>
            daoFuture.map(implicit dao => dao.enterPublicRoom(playersNumber)(Participant(user.username, addresses._1.address), addresses._2)
              .onComplete {
                failureHandler orElse successHandler(_ => handlePublicRoomFilling(playersNumber) map (_ => sendResponse(OK)))
              })

          case _ =>
            log.warn("The number of players in url and player/notification address in body are required for public room entering")
            sendResponse(BAD_REQUEST, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_NEEDED_PLAYERS} or ${Address.FIELD_ADDRESS}")
        }
      }))
  }

  /**
    * Handles retrieving info of a public room
    */
  private def publicRoomInfoHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Public Room Info Request received...")
    request.checkAuthenticationOrReject.map(_ => {
      (try getRequestParameter(Room.FIELD_NEEDED_PLAYERS).map(_.toInt)
      catch {
        case _: Throwable => None
      }) match {
        case Some(playersNumber) =>
          daoFuture.map(_.publicRoomInfo(playersNumber).map(_._1).onComplete {
            failureHandler orElse { case Success(room) => sendResponse(OK, room.toJson.encode()) }
          })
        case None =>
          log.warn("The number of players in url is required for public room info retrieval")
          sendResponse(BAD_REQUEST, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_NEEDED_PLAYERS}")
      }
    })
  }

  /**
    * Handles exiting a public room
    */
  private def exitPublicRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Exit Public Room Request received...")
    request.checkAuthenticationOrReject.map(implicit user => {
      (try getRequestParameter(Room.FIELD_NEEDED_PLAYERS).map(_.toInt)
      catch {
        case _: Throwable => None
      }) match {
        case Some(roomId) =>
          daoFuture.map(_.exitPublicRoom(roomId).onComplete {
            failureHandler orElse successHandler(_ => sendResponse(OK))
          })
        case None =>
          log.warn("The number of players in url is required for public room exiting")
          sendResponse(BAD_REQUEST, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_NEEDED_PLAYERS}")
      }
    })
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
      onRetrievedRoomAction = (roomDAO deleteRoom roomID)
        .map(_ => sendResponse(OK)))(communicationStrategy, executionContext)
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
      onRetrievedRoomAction = (roomDAO deleteAndRecreatePublicRoom playersNumber)
        .map(_ => sendResponse(OK)))(communicationStrategy, executionContext)
  }

  /**
    * Describes how to behave when a room is filled out
    *
    * @param roomInformationFuture the future that will contain the room and addresses of users to contact
    * @param onRetrievedRoomAction the action to do if the room is full
    * @return a future that completes when all players received addresses
    */
  private def handleRoomFilling(roomInformationFuture: Future[(Room, Seq[Address])], onRetrievedRoomAction: => Future[Unit])
                               (implicit communicationStrategy: RoomReceiverApiWrapper, executionContext: ExecutionContext): Future[Unit] = {
    roomInformationFuture
      .map(roomAndAddresses => roomAndAddresses._1)
      .filter(roomIsFull)
      .flatMap(_ => {
        onRetrievedRoomAction
        sendParticipantAddresses(roomInformationFuture.value.get.get)(communicationStrategy, executionContext)
      }).transform {
      case Success(_) => Failure(new Exception()) // if operation successful, outer response sending should block
      case Failure(_: NoSuchElementException) => Success(()) // if room wasn't full, let outer response sending happen
      case Failure(ex) => log.error("Error handling Room Filling", ex); Success(())
      // unexpected error, log and let outer response sending happen
    }
  }

  /**
    * Method to communicate to clients that the game can start because of reaching the specified number of players
    *
    * @param roomInformation the room where players are waiting in
    */
  private def sendParticipantAddresses(roomInformation: (Room, Seq[Address]))
                                      (implicit communicationStrategy: RoomReceiverApiWrapper, executionContext: ExecutionContext): Future[Unit] = {
    log.info(s"Preparing to send participant list to room ${roomInformation._1.name} (with id:${roomInformation._1.identifier}) participants ...")
    val notificationAddresses = for (notificationAddress <- roomInformation._2) yield notificationAddress
    val players = for (player <- roomInformation._1.participants) yield player
    log.info(s"Participant notification addresses to contact: $notificationAddresses")
    log.info(s"Information to send -> $players")
    Future.sequence {
      notificationAddresses map (notificationAddress => communicationStrategy.sendParticipants(notificationAddress.address, players))
    } map (_ => log.info("Information sent."))
  }

  /**
    * @return the common handler for failures in this service
    */
  private def failureHandler(implicit routingContext: RoutingContext): PartialFunction[Try[_], Unit] = {
    case Failure(ex: NoSuchElementException) => sendResponse(NOT_FOUND, ex.getMessage)
    case Failure(ex) => sendResponse(BAD_REQUEST, ex.getMessage)
  }

  /**
    * @return the common handler for success, that does the specified action with result
    */
  private def successHandler[T](action: T => Unit)(implicit routingContext: RoutingContext): PartialFunction[Try[T], Unit] = {
    case Success(successResult) => action(successResult)
  }
}


/**
  * Companion object
  */
object RoomsServiceVerticle {

  def apply(implicit validationStrategy: Validation[String, User],
            clientCommunicationStrategy: RoomReceiverApiWrapper): RoomsServiceVerticle =
    new RoomsServiceVerticle()(validationStrategy, clientCommunicationStrategy)

  private val INVALID_PARAMETER_ERROR: String = "Invalid parameters: "

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
        Some((userAddressJson.toAddress, userNotificationAddressJson.toAddress))
      } else None
    } catch {
      case _: Throwable => None
    }
  }

  /**
    * Describes when a room is full
    */
  private def roomIsFull(room: Room): Boolean = room.participants.size == room.neededPlayersNumber
}