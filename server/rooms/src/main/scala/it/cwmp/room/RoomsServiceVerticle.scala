package it.cwmp.room

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.authentication.Validation
import it.cwmp.model.{Address, Room, User}
import it.cwmp.room.RoomsServiceVerticle._
import it.cwmp.utils.HttpUtils
import javax.xml.ws.http.HTTPException

import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

/**
  * Class that implements the Rooms micro-service
  *
  * @author Enrico Siboni
  */
case class RoomsServiceVerticle(validationStrategy: Validation[String, User]) extends ScalaVerticle {

  private var daoFuture: Future[RoomLocalDAO] = _

  override def startFuture(): Future[_] = {
    val storageHelper = RoomLocalDAO(vertx)
    daoFuture = storageHelper.initialize().map(_ => storageHelper)

    val router = Router.router(vertx)
    router post API_CREATE_PRIVATE_ROOM_URL handler createPrivateRoomHandler
    router put API_ENTER_PRIVATE_ROOM_URL handler enterPrivateRoomHandler
    router get API_PRIVATE_ROOM_INFO_URL handler privateRoomInfoHandler
    router delete API_EXIT_PRIVATE_ROOM_URL handler exitPrivateRoomHandler

    router get API_LIST_PUBLIC_ROOMS_URL handler listPublicRoomsHandler
    router put API_ENTER_PUBLIC_ROOM_URL handler enterPublicRoomHandler
    router get API_PUBLIC_ROOM_INFO_URL handler publicRoomInfoHandler
    router delete API_EXIT_PUBLIC_ROOM_URL handler exitPublicRoomHandler

    vertx
      .createHttpServer()
      .requestHandler(router.accept _)
      .listenFuture(DEFAULT_PORT)
  }

  /**
    * Handles the creation of a private room
    */
  private def createPrivateRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    routingContext.request().bodyHandler(body => {
      validateUserOrSendError.map(_ =>
        extractIncomingRoomFromBody(body) match {
          case Some((roomName, neededPlayers)) =>
            daoFuture.map(_.createRoom(roomName, neededPlayers)
              .onComplete {
                case Success(generatedID) => sendResponse(201, Some(generatedID))
                case Failure(ex) => sendResponse(400, Some(ex.getMessage))
              })
          case None =>
            sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR no Room JSON in body"))
        })
    })

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

  private def enterPrivateRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => {
      // TODO: GET_USER
      implicit val testUser: User with Address = User(s"Test${Random.nextInt()}", "fakeAddress")
      extractRequestParam(Room.FIELD_IDENTIFIER) match {
        case Some(roomID) if roomID == "public" =>
          daoFuture.map(_.enterPublicRoom(roomID.toInt).onComplete {
            case Success(_) => sendResponse(200, None)
            case Failure(_) => sendResponse(500, Some(INTERNAL_SERVER_ERROR))
          })

        case Some(roomID) =>
          daoFuture.map(_.enterRoom(roomID).onComplete {
            case Success(_) => sendResponse(200, None)
            case Failure(_) => sendResponse(404, Some(RESOURCE_NOT_FOUND))
          })

        case None => sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR ${Room.FIELD_IDENTIFIER}"))
      }
    })
  }

  private def privateRoomInfoHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => {
      extractRequestParam(Room.FIELD_IDENTIFIER) match {
        case Some(roomId) =>
          daoFuture.map(_.roomInfo(roomId).onComplete {
            case Success(room) =>
              import Room.Converters._
              sendResponse(200, Some(room.toJson.encode()))
            case _ => sendResponse(404, Some(RESOURCE_NOT_FOUND))
          })
        case None => sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR ${Room.FIELD_IDENTIFIER}"))
      }
    })
  }

  private def exitPrivateRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => sendResponse(200, Some("TODO")))
  }

  private def listPublicRoomsHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => {
      daoFuture.map(_.listPublicRooms().onComplete {
        case Success(rooms) =>
          import Room.Converters._
          sendResponse(200, Some(Json.arr(rooms.map(_.toJson)).encode()))

        case Failure(_) => sendResponse(500, Some(INTERNAL_SERVER_ERROR))
      })
    })
  }

  private def enterPublicRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => sendResponse(200, Some("TODO")))
  }

  private def publicRoomInfoHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => sendResponse(200, Some("TODO")))
  }

  private def exitPublicRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => sendResponse(200, Some("TODO")))
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
    HttpUtils.getRequestAuthorizationHeader(routingContext.request()) match {
      case None =>
        sendResponse(400, Some(TOKEN_NOT_PROVIDED_OR_INVALID))
        Future.failed(new IllegalAccessException(TOKEN_NOT_PROVIDED_OR_INVALID))

      case Some(authorizationToken) =>
        validationStrategy.validate(authorizationToken)
          .recoverWith {
            case ex: HTTPException =>
              sendResponse(ex.getStatusCode, Some(TOKEN_NOT_PROVIDED_OR_INVALID))
              Future.failed(new IllegalAccessException(TOKEN_NOT_PROVIDED_OR_INVALID))
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

  val DEFAULT_PORT = 8667

  val API_CREATE_PRIVATE_ROOM_URL = "/api/rooms"
  val API_ENTER_PRIVATE_ROOM_URL = s"/api/rooms/:${Room.FIELD_IDENTIFIER}"
  val API_PRIVATE_ROOM_INFO_URL = s"/api/rooms/:${Room.FIELD_IDENTIFIER}"
  val API_EXIT_PRIVATE_ROOM_URL = s"/api/rooms/:${Room.FIELD_IDENTIFIER}/self"

  val API_LIST_PUBLIC_ROOMS_URL = "/api/rooms"
  val API_ENTER_PUBLIC_ROOM_URL = s"/api/rooms/public/:${Room.FIELD_NEEDED_PLAYERS}"
  val API_PUBLIC_ROOM_INFO_URL = s"/api/rooms/public/:${Room.FIELD_NEEDED_PLAYERS}"
  val API_EXIT_PUBLIC_ROOM_URL = s"/api/rooms/public/:${Room.FIELD_NEEDED_PLAYERS}/self"

  private val USER_NOT_AUTHENTICATED = "User is not authenticated"
  private val TOKEN_NOT_PROVIDED_OR_INVALID = "Token not provided or invalid"
  private val INVALID_PARAMETER_ERROR = "Invalid parameters: "
  private val INTERNAL_SERVER_ERROR = "Internal server error"
  private val RESOURCE_NOT_FOUND = "Resource not found"

  /**
    * @param routingContext the routing context on which to extract
    * @return the extracted room name
    */
  private def extractRequestParam(paramName: String)(implicit routingContext: RoutingContext): Option[String] = {
    routingContext.request().getParam(paramName)
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

    message match {
      case Some(messageString) => response.end(messageString)
      case None => response.end()
    }
  }
}