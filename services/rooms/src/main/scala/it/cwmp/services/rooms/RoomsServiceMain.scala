package it.cwmp.services.rooms

import com.typesafe.scalalogging.Logger
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.scala.core.Vertx
import it.cwmp.services.wrapper.{AuthenticationApiWrapper, RoomReceiverApiWrapper}

import scala.util.{Failure, Success}

/**
  * Object that implements the Rooms micro-service entry-point
  *
  * @author Enrico Siboni
  */
object RoomsServiceMain extends App {
  private val logger = Logger[RoomsServiceMain.type]
  private val vertx: Vertx = Vertx.vertx()

  logger.info("Deploying RoomService...")
  vertx.deployVerticleFuture(RoomsServiceVerticle(AuthenticationApiWrapper(), RoomReceiverApiWrapper()))
    .onComplete {
      case Success(_) => logger.info("RoomsService up and running!")
      case Failure(ex) => logger.error("Error deploying RoomsService", ex)
    }(VertxExecutionContext(vertx.getOrCreateContext()))

}
