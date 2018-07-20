package it.cwmp.services.rooms

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.{Address, Participant, Room, User}
import it.cwmp.services.rooms.ServerParameters._
import it.cwmp.services.wrapper.RoomReceiverApiWrapper
import it.cwmp.utils.{Logging, Validation, VertxServer}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Class that implements the Rooms micro-service
  *
  * @author Enrico Siboni
  */
case class RoomsServiceVerticle(validationStrategy: Validation[String, User],
                                implicit val clientCommunicationStrategy: RoomReceiverApiWrapper)
  extends VertxServer with RoomsServiceUtils with Logging {

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
    val roomDao = RoomsLocalDAO()
    daoFuture = roomDao.initialize().map(_ => roomDao)
    daoFuture
  }

  /**
    * Handles the creation of a private room
    */
  private def createPrivateRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Create Private Room Request received...")
    routingContext.request().bodyHandler(body =>
      validateUserOrSendError.map(_ =>
        extractIncomingRoomFromBody(body) match {
          case Some((roomName, neededPlayers)) =>
            daoFuture.map(_.createRoom(roomName, neededPlayers)
              .onComplete {
                case Success(generatedID) => sendResponse(201, generatedID)
                case Failure(ex) => sendResponse(400, ex.getMessage)
              })
          case None =>
            log.warn("Request body for room creation is required!")
            sendResponse(400, s"$INVALID_PARAMETER_ERROR no Room JSON in body")
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
    routingContext.request().bodyHandler(body =>
      validateUserOrSendError.map(user => {
        (getRequestParameter(Room.FIELD_IDENTIFIER), extractAddressesFromBody(body)) match {
          case (Some(roomID), Some(addresses)) =>
            daoFuture.map(implicit dao => dao.enterRoom(roomID)(Participant(user.username, addresses._1.address), addresses._2).onComplete {
              case Success(_) => handlePrivateRoomFilling(roomID)
                .transform({
                  case Success(_) => Failure(new Exception())
                  case Failure(_: NoSuchElementException) => Success(Unit)
                  case Failure(ex) => log.error("Error handling Private Room Filling", ex); Success(Unit)
                })
                .map(_ => sendResponse(200))
              case Failure(ex: NoSuchElementException) => sendResponse(404, ex.getMessage)
              case Failure(ex) => sendResponse(400, ex.getMessage)
            })

          case _ =>
            log.warn("Player and notification address are required for entering a room!")
            sendResponse(400, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_IDENTIFIER} or ${Address.FIELD_ADDRESS}")
        }
      }))
  }

  /**
    * Handles retrieving info of a private room
    */
  private def privateRoomInfoHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Private Room Info Request received...")
    validateUserOrSendError.map(_ => {
      getRequestParameter(Room.FIELD_IDENTIFIER) match {
        case Some(roomId) =>
          daoFuture.map(_.roomInfo(roomId).map(_._1).onComplete {
            case Success(room) =>
              import Room.Converters._
              sendResponse(200, room.toJson.encode())
            case Failure(ex: NoSuchElementException) => sendResponse(404, ex.getMessage)
            case Failure(ex) => sendResponse(400, ex.getMessage)
          })
        case None => sendResponse(400, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_IDENTIFIER}")
      }
    })
  }

  /**
    * Handles exititng a private room
    */
  private def exitPrivateRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Exit Private Room Request received...")
    validateUserOrSendError.map(implicit user => {
      getRequestParameter(Room.FIELD_IDENTIFIER) match {
        case Some(roomId) =>
          daoFuture.map(_.exitRoom(roomId).onComplete {
            case Success(_) => sendResponse(200)
            case Failure(ex: NoSuchElementException) => sendResponse(404, ex.getMessage)
            case Failure(ex) => sendResponse(400, ex.getMessage)
          })
        case None => sendResponse(400, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_IDENTIFIER}")
      }
    })
  }

  /**
    * Handles listing of public rooms
    */
  private def listPublicRoomsHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("List Public Rooms Request received...")
    validateUserOrSendError.map(_ => {
      daoFuture.map(_.listPublicRooms().onComplete {
        case Success(rooms) =>
          import Room.Converters._
          val jsonArray = rooms.foldLeft(Json emptyArr())(_ add _.toJson)
          sendResponse(200, jsonArray encode())
        case Failure(ex) => sendResponse(400, ex.getMessage)
      })
    })
  }

  /**
    * Handles entering in a public room
    */
  private def enterPublicRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Enter Public Room Request received...")
    routingContext.request().bodyHandler(body =>
      validateUserOrSendError.map(user => {
        val playersNumberOption = try getRequestParameter(Room.FIELD_NEEDED_PLAYERS).map(_.toInt)
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
                  case Failure(ex) => log.error("Error handling Public Room Filling", ex); Success(Unit)
                })
                .map(_ => sendResponse(200))
              case Failure(ex: NoSuchElementException) => sendResponse(404, ex.getMessage)
              case Failure(ex) => sendResponse(400, ex.getMessage)
            })

          case _ =>
            log.warn("The number of players in url and player/notification address in body are required for public room entering")
            sendResponse(400, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_NEEDED_PLAYERS} or ${Address.FIELD_ADDRESS}")
        }
      }))
  }

  /**
    * Handles retrieving info of a public room
    */
  private def publicRoomInfoHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Public Room Info Request received...")
    validateUserOrSendError.map(_ => {
      (try getRequestParameter(Room.FIELD_NEEDED_PLAYERS).map(_.toInt)
      catch {
        case _: Throwable => None
      }) match {
        case Some(playersNumber) =>
          daoFuture.map(_.publicRoomInfo(playersNumber).map(_._1).onComplete {
            case Success(room) =>
              import Room.Converters._
              sendResponse(200, room.toJson.encode())
            case Failure(ex: NoSuchElementException) => sendResponse(404, ex.getMessage)
            case Failure(ex) => sendResponse(400, ex.getMessage)
          })
        case None =>
          log.warn("The number of players in url is required for public room info retrieval")
          sendResponse(400, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_NEEDED_PLAYERS}")
      }
    })
  }

  /**
    * Handles exiting a public room
    */
  private def exitPublicRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Exit Public Room Request received...")
    validateUserOrSendError.map(implicit user => {
      (try getRequestParameter(Room.FIELD_NEEDED_PLAYERS).map(_.toInt)
      catch {
        case _: Throwable => None
      }) match {
        case Some(roomId) =>
          daoFuture.map(_.exitPublicRoom(roomId).onComplete {
            case Success(_) => sendResponse(200)
            case Failure(ex: NoSuchElementException) => sendResponse(404, ex.getMessage)
            case Failure(ex) => sendResponse(400, ex.getMessage)
          })
        case None =>
          log.warn("The number of players in url is required for public room exiting")
          sendResponse(400, s"$INVALID_PARAMETER_ERROR ${Room.FIELD_NEEDED_PLAYERS}")
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
        log.warn("No authorization header in request")
        sendResponse(400, TOKEN_NOT_PROVIDED_OR_INVALID)
        Future.failed(new IllegalAccessException(TOKEN_NOT_PROVIDED_OR_INVALID))

      case Some(authorizationToken) =>
        validationStrategy.validate(authorizationToken)
          .recoverWith {
            case HTTPException(statusCode, errorMessage) =>
              sendResponse(statusCode, errorMessage)
              Future.failed(new IllegalAccessException(errorMessage))
          }
    }
  }
}