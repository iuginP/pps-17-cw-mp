package it.cwmp.server

import it.cwmp.services.VertxInstance
import it.cwmp.services.authentication.AuthenticationServiceVerticle
import it.cwmp.services.rooms.RoomsServiceVerticle
import it.cwmp.services.wrapper.{AuthenticationApiWrapper, RoomReceiverApiWrapper}
import it.cwmp.utils.Logging

import scala.util.{Failure, Success}

object ServerMain extends App with VertxInstance with Logging {

  log.info("Deploying AuthenticationService... ")
  vertx.deployVerticleFuture(AuthenticationServiceVerticle())
    .andThen {
      case Success(_) => log.info("AuthenticationService up and running!")
      case Failure(ex) => log.info("Error deploying AuthenticationService", ex)
    }
    .map(_ => {
      log.info("Deploying RoomsService... ")
      vertx.deployVerticleFuture(RoomsServiceVerticle(AuthenticationApiWrapper(), RoomReceiverApiWrapper()))
    })
    .andThen {
      case Success(_) => log.info("RoomsService up and running!")
      case Failure(ex) => log.info("Error deploying RoomsService", ex)
    }
}
