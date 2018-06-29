package it.cwmp.room

import io.vertx.core.Handler
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.model.User

import scala.concurrent.Future
import scala.util.Random

/**
  * Class that implements the Rooms micro-service
  *
  * @author Enrico Siboni
  */
class RoomsServiceVerticle extends ScalaVerticle {

  private val ROOM_NAME_PARAM = "room_name"

  private val USER_NOT_AUTHENTICATED = "User is not authenticated"
  private val TOKEN_NOT_PROVIDED = "Token not provided"
  private val INVALID_PARAMETER_ERROR = "Invalid parameters:"
  private val INTERNAL_SERVER_ERROR = "Internal server error"
  private val RESOURCE_NOT_FOUND = "Resource not found"

  private var daoFuture: Future[RoomsDAO] = _

  override def startFuture(): Future[_] = {

    val storageWrapper = RoomsDAO(vertx)
    daoFuture = storageWrapper.initialize().map(_ => storageWrapper)

    val router = Router.router(vertx)
    router post "/api/rooms" handler createRoomHandler
    router get "/api/rooms" handler listRoomsHandler
    router get "/api/rooms/public" handler enterPublicRoomHandler
    router get "/api/rooms/:room" handler enterRoomHandler
    router get "/api/rooms/:room/info" handler retrieveRoomInfoHandler

    vertx
      .createHttpServer()
      .requestHandler(router.accept _)
      .listenFuture(8667, "127.0.0.1")
  }

  private def createRoomHandler: Handler[RoutingContext] = routingContext => {
    if (checkUserAuthentication(routingContext)) {
      getStringFromBodyOrSendError(routingContext, ROOM_NAME_PARAM).foreach(roomName => {
        daoFuture.map(_.createRoom(roomName).onComplete(result => {
          if (result.isSuccess)
            routingContext.response().setStatusCode(201)
          else
            routingContext.response().setStatusCode(500).end(INTERNAL_SERVER_ERROR)
        }))
      })
    }
  }

  private def listRoomsHandler: Handler[RoutingContext] = routingContext => {
    if (checkUserAuthentication(routingContext)) {
      daoFuture.map(_.listRooms().onComplete(result => {
        if (result.isSuccess) {
          import RoomUtils.RichRoom

          routingContext.response().end(
            Json.arr(result.get.map(_.toJson))
              .encode())
        } else {
          routingContext.response().setStatusCode(500).end(INTERNAL_SERVER_ERROR)
        }
      }))
    }
  }

  private def enterPublicRoomHandler: Handler[RoutingContext] = routingContext => {
    if (checkUserAuthentication(routingContext)) {
      // TODO: GET_USER
      val testUser = User(s"Test${Random.nextInt()}")
      daoFuture.map(_.enterPublicRoom(testUser).onComplete(result => {
        if (result.isSuccess) {
          routingContext.response().setStatusCode(200).end()
        } else {
          routingContext.response().setStatusCode(500).end(INTERNAL_SERVER_ERROR)
        }
      }))
    }
  }

  private def enterRoomHandler: Handler[RoutingContext] = routingContext => {
    if (checkUserAuthentication(routingContext)) {
      getStringFromBodyOrSendError(routingContext, ROOM_NAME_PARAM).foreach(roomName => {
        // TODO: GET_USER
        val testUser = User(s"Test${Random.nextInt()}")
        daoFuture.map(_.enterRoom(roomName, testUser).onComplete(result => {
          if (result.isSuccess) {
            routingContext.response().setStatusCode(200).end()
          } else {
            routingContext.response().setStatusCode(404).end(RESOURCE_NOT_FOUND)
          }
        }))
      })
    }
  }

  private def retrieveRoomInfoHandler: Handler[RoutingContext] = routingContext => {
    if (checkUserAuthentication(routingContext)) {
      val maybeRoomName = getStringFromBodyOrSendError(routingContext, ROOM_NAME_PARAM)
      maybeRoomName.foreach(roomName => {
        daoFuture.map(_.getRoomInfo(roomName).onComplete(result => {
          if (result.isSuccess) {
            import RoomUtils.RichRoom
            routingContext.response().setStatusCode(200).end(
              result.get.toJson.encode())
          } else {
            routingContext.response().setStatusCode(404).end(RESOURCE_NOT_FOUND)
          }
        }))
      })
    }
  }

  /**
    * Checks whether the user is authenticated;
    * if token not provided sends back 400
    * if token invalid sends back 401
    *
    * @param routingContext the context in which to check
    */
  private def checkUserAuthentication(routingContext: RoutingContext): Boolean = {
    // routingContext.response().setStatusCode(400).end(TOKEN_NOT_PROVIDED)
    // routingContext.response().setStatusCode(401).end(USER_NOT_AUTHENTICATED)
    true // TODO: chek authentication
  }

  /**
    * @param routingContext the routing context on which to extract
    * @return the extracted room name
    */
  private def getStringFromBodyOrSendError(routingContext: RoutingContext, paramName: String): Option[String] = {
    val maybeString = routingContext.request().getParam(paramName)
    if (maybeString.isEmpty) {
      routingContext.response()
        .setStatusCode(405).end(s"$INVALID_PARAMETER_ERROR $ROOM_NAME_PARAM")
    }
    maybeString
  }
}
