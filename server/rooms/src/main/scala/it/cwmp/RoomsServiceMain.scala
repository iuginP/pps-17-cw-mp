package it.cwmp

import io.vertx.scala.core.Vertx
import it.cwmp.authentication.AuthenticationService
import it.cwmp.controller.client.ClientCommunication
import it.cwmp.controller.rooms.RoomsServiceVerticle

/**
  * Object that implements the Rooms micro-service entry-point
  *
  * @author Enrico Siboni
  */
object RoomsServiceMain extends App {

  private implicit val vertx: Vertx = Vertx.vertx()
  vertx.deployVerticle(RoomsServiceVerticle(AuthenticationService(), ClientCommunication()))

  println("Deploying RoomServiceVerticle... ") // TODO replace with logger logging
}
