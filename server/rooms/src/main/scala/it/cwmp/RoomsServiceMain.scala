package it.cwmp

import io.vertx.scala.core.Vertx
import it.cwmp.room.RoomsServiceVerticle

/**
  * Object that implements the Rooms micro-service entry-point
  *
  * @author Enrico Siboni
  */
object RoomsServiceMain extends App {

  Vertx.vertx().deployVerticle(new RoomsServiceVerticle)

  println("Deploying RoomServiceVerticle... ") // TODO replace with logger logging
}
