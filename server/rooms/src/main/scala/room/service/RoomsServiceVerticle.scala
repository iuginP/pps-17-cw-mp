package room.service

import io.vertx.core.Handler
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.ext.web.{Router, RoutingContext}
import room.Room

import scala.concurrent.{Future, Promise}

/**
  * Class that implements the Rooms micro-service
  *
  * @author Enrico Siboni
  */
class RoomsServiceVerticle extends ScalaVerticle {

  override def startFuture(): Future[_] = {

    val router = Router.router(vertx)
    router get "/api/rooms" handler listRoomsHandler
    router post "/api/rooms" handler createRoomHandler
    router get "/api/rooms/public" handler enterPublicRoomHandler
    router get "/api/rooms/:room" handler enterRoomHandler
    router get "/api/rooms/:room/info" handler retrieveRoomInfoHandler


    vertx
      .createHttpServer()
      .requestHandler(router.accept _)
      .listenFuture(8667, "127.0.0.1")
  }

  private def listRoomsHandler: Handler[RoutingContext] = routingContext => {
    getRooms.future.onComplete(maybeRooms => {
      if (maybeRooms.isSuccess) {
        routingContext.response().end(maybeRooms.get.toString())
      } else {
        routingContext.response().end("Error retrieving rooms!")
      }
    })
  }

  private def createRoomHandler: Handler[RoutingContext] = routingContext => {
    // TODO:
    routingContext.response().end()
  }

  private def enterPublicRoomHandler: Handler[RoutingContext] = routingContext => {
    // TODO:
    routingContext.response().end()
  }

  private def enterRoomHandler: Handler[RoutingContext] = routingContext => {
    // TODO:
    routingContext.response().end()
  }

  private def retrieveRoomInfoHandler: Handler[RoutingContext] = routingContext => {
    // TODO:
    routingContext.response().end()
  }

  /**
    * Retrieves rooms from DB asynchronously
    *
    * @return the promise about the retrieved Rooms
    */
  private def getRooms: Promise[Seq[Room]] = {
    val promisedRooms: Promise[Seq[Room]] = Promise()

    // Blocking operation here, and at the end promise.success call
    promisedRooms.success(Seq(Room("First"), Room("Second")))

    promisedRooms
  }
}
