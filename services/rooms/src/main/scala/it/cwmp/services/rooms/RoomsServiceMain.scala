package it.cwmp.services.rooms

import it.cwmp.services.VertxInstance
import it.cwmp.services.wrapper.{AuthenticationApiWrapper, RoomReceiverApiWrapper}
import it.cwmp.utils.Logging

import scala.util.{Failure, Success}

/**
  * Object that implements the Rooms micro-service entry-point
  *
  * @author Enrico Siboni
  */
object RoomsServiceMain extends App with VertxInstance with Logging {
  log.info("Deploying RoomService...")
  vertx.deployVerticleFuture(RoomsServiceVerticle(AuthenticationApiWrapper(), RoomReceiverApiWrapper()))
    .onComplete {
      case Success(_) => log.info("RoomsService up and running!")
      case Failure(ex) => log.info("Error deploying RoomsService", ex)
    }
}
