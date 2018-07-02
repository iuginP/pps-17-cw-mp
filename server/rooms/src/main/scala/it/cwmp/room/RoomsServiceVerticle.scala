package it.cwmp.room

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.Handler
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.model.User
import it.cwmp.utils.HttpUtils

import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

/**
  * Class that implements the Rooms micro-service
  *
  * @author Enrico Siboni
  */
class RoomsServiceVerticle extends ScalaVerticle {

  private val ROOM_NAME_PARAM = ":room"

  private val USER_NOT_AUTHENTICATED = "User is not authenticated"
  private val TOKEN_NOT_PROVIDED_OR_INVALID = "Token not provided or invalid"
  private val INVALID_PARAMETER_ERROR = "Invalid parameters:"
  private val INTERNAL_SERVER_ERROR = "Internal server error"
  private val RESOURCE_NOT_FOUND = "Resource not found"

  private var daoFuture: Future[RoomsDAO] = _

  override def startFuture(): Future[_] = {

    val storageHelper = RoomsDAO(vertx)
    daoFuture = storageHelper.initialize().map(_ => storageHelper)

    val router = Router.router(vertx)
    router post "/api/rooms" handler createRoomHandler
    router get "/api/rooms" handler listRoomsHandler
    router get "/api/rooms/public" handler enterPublicRoomHandler // TODO:  remove this and check for "public" inside :room
    router get "/api/rooms/:room" handler enterRoomHandler
    router get "/api/rooms/:room/info" handler retrieveRoomInfoHandler

    vertx
      .createHttpServer()
      .requestHandler(router.accept _)
      .listenFuture(8667)
  }

  private def createRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => {
      val roomName = getRequestParam(ROOM_NAME_PARAM)
        .getOrElse(s"Room${Random.nextInt(Int.MaxValue)}") // TODO: check if random generated room is already present

      daoFuture.map(_.createRoom(roomName).onComplete {
        case Success(_) => sendResponse(201, None)
        case Failure(_) => sendResponse(500, Some(INTERNAL_SERVER_ERROR))
      })
    })
  }

  private def listRoomsHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => {
      daoFuture.map(_.listRooms().onComplete {
        case Success(rooms) =>
          import RoomUtils.RichRoom
          sendResponse(200, Some(Json.arr(rooms.map(_.toJson)).encode()))

        case Failure(_) => sendResponse(500, Some(INTERNAL_SERVER_ERROR))
      })
    })
  }

  private def enterPublicRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => {
      // TODO: GET_USER
      val testUser = User(s"Test${Random.nextInt()}")
      daoFuture.map(_.enterPublicRoom(testUser).onComplete {
        case Success(_) => sendResponse(200, None)
        case Failure(_) => sendResponse(500, Some(INTERNAL_SERVER_ERROR))
      })
    })
  }

  private def enterRoomHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => {
      getRequestParam(ROOM_NAME_PARAM) match {
        case Some(roomName) =>
          // TODO: GET_USER
          val testUser = User(s"Test${Random.nextInt()}")
          daoFuture.map(_.enterRoom(roomName, testUser).onComplete {
            case Success(_) => sendResponse(200, None)
            case Failure(_) => sendResponse(404, Some(RESOURCE_NOT_FOUND))
          })

        case None => sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR $ROOM_NAME_PARAM")) // TODO: refactor with below method
      }
    })
  }

  private def retrieveRoomInfoHandler: Handler[RoutingContext] = implicit routingContext => {
    validateUserOrSendError.map(_ => {
      getRequestParam(ROOM_NAME_PARAM) match {
        case Some(roomName) =>
          daoFuture.map(_.getRoomInfo(roomName).onComplete {
            case Success(room) =>
              import RoomUtils.RichRoom
              sendResponse(200, Some(room.toJson.encode()))
            case Failure(_) => sendResponse(404, Some(RESOURCE_NOT_FOUND))
          })
        case None => sendResponse(400, Some(s"$INVALID_PARAMETER_ERROR $ROOM_NAME_PARAM"))
      }
    })
  }

  /**
    * Checks whether the user is authenticated;
    * if token not provided sends back 400
    * if token invalid sends back 401
    *
    * @param routingContext the context in which to check
    */
  private def validateUserOrSendError(implicit routingContext: RoutingContext): Future[User] = {
    (for (
      httpToken <- routingContext.request().headers().get(HttpHeaderNames.AUTHORIZATION.toString);
      jwtOption = HttpUtils.buildJwtAuthentication(httpToken);
      jwToken <- jwtOption
    ) yield {

      // TODO: Utilizzare l'AuthenticationServiceHelper
      val webClient = WebClient.create(vertx, WebClientOptions().setDefaultHost("127.0.0.1").setDefaultPort(8666))
      webClient.get("/api/validate")
        .putHeader(HttpHeaderNames.AUTHORIZATION.toString, jwToken)
        .sendFuture()
        .flatMap(response => response.statusCode() match {
          case 200 => Future.successful(User(response.bodyAsString.get))
          case 400 =>
            sendResponse(400, Some(TOKEN_NOT_PROVIDED_OR_INVALID))
            Future.failed(new Exception(TOKEN_NOT_PROVIDED_OR_INVALID))
          case 401 =>
            sendResponse(401, Some(USER_NOT_AUTHENTICATED))
            Future.failed(new Exception(USER_NOT_AUTHENTICATED))
        })
    }) match {
      case Some(userFuture) => userFuture
      case None =>
        sendResponse(400, Some(TOKEN_NOT_PROVIDED_OR_INVALID))
        Future.failed(new Exception(TOKEN_NOT_PROVIDED_OR_INVALID))
    }
  }

  /**
    * @param routingContext the routing context on which to extract
    * @return the extracted room name
    */
  private def getRequestParam(paramName: String)(implicit routingContext: RoutingContext): Option[String] = {
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
