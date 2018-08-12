package it.cwmp.server

import it.cwmp.services.authentication.AuthenticationServiceVerticle
import it.cwmp.services.discovery.DiscoveryServiceVerticle
import it.cwmp.services.rooms.RoomsServiceVerticle
import it.cwmp.services.wrapper.{AuthenticationApiWrapper, DiscoveryApiWrapper, RoomReceiverApiWrapper, RoomsApiWrapper}
import it.cwmp.services.{VertxInstance, authentication, rooms}
import it.cwmp.utils.Logging

import scala.util.{Failure, Success}

/**
  * Handle class to start all server parts
  */
object ServerMain extends App with VertxInstance with Logging {

  log.info("Deploying DiscoveryService... ")
  val discoveryApiWrapper: DiscoveryApiWrapper = DiscoveryApiWrapper()
  vertx.deployVerticleFuture(DiscoveryServiceVerticle())
    .andThen {
      case Success(_) => log.info("DiscoveryService up and running!")
      case Failure(ex) => log.info("Error deploying DiscoveryService", ex)
    }
    .map(_ => {
      log.info("Deploying AuthenticationService... ")
      vertx.deployVerticleFuture(AuthenticationServiceVerticle())
    })
    .andThen {
      case Success(_) =>
        log.info("AuthenticationService up and running!")
        discoveryApiWrapper.publish(
          authentication.Service.DISCOVERY_NAME,
          AuthenticationApiWrapper.DEFAULT_HOST,
          authentication.Service.DEFAULT_PORT.toInt)
      case Failure(ex) => log.info("Error deploying AuthenticationService", ex)
    }
    .map(_ => {
      log.info("Deploying RoomsService... ")
      vertx.deployVerticleFuture(RoomsServiceVerticle(AuthenticationApiWrapper(), RoomReceiverApiWrapper()))
    })
    .andThen {
      case Success(_) =>
        log.info("RoomsService up and running!")
        discoveryApiWrapper.publish(
          rooms.Service.DISCOVERY_NAME,
          RoomsApiWrapper.DEFAULT_HOST,
          rooms.Service.DEFAULT_PORT.toInt)
      case Failure(ex) => log.info("Error deploying RoomsService", ex)
    }
}
