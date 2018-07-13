package it.cwmp

import io.vertx.scala.core.Vertx
import it.cwmp.authentication.AuthenticationService
import it.cwmp.controller.client.RoomReceiverApiWrapper
import it.cwmp.controller.rooms.RoomsServiceVerticle

/**
  * Object that implements the Rooms micro-service entry-point
  *
  * @author Enrico Siboni
  */
object RoomsServiceMain extends App {

  private val vertx: Vertx = Vertx.vertx()
  vertx.deployVerticle(RoomsServiceVerticle(AuthenticationService(), RoomReceiverApiWrapper()))

  println("Deploying RoomServiceVerticle... ") // TODO replace with logger logging
}
