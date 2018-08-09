package it.cwmp.client

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import it.cwmp.client.controller.{ApiClientActor, ClientControllerActor}
import it.cwmp.services.VertxInstance
import it.cwmp.services.wrapper.{AuthenticationApiWrapper, DiscoveryApiWrapper, RoomsApiWrapper}
import it.cwmp.utils.Logging

import scala.util.Failure

/**
  * The client entry point
  */
object ClientMain extends App with VertxInstance with Logging {

  // Terminal arguments check
  require(args.length == 2,
    """
      Invalid number of arguments: I need
      (1) the host address of the discovery service
      (2) the port of the discovery service
    """)

  val discoveryHost: String = args(0)
  val discoveryPort: Int = args(1) toInt

  val APP_NAME = "CellWarsClient"

  private val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=0").withFallback(ConfigFactory.load())
  private val system = ActorSystem(APP_NAME, config)
  private val discoveryApiWrapper: DiscoveryApiWrapper = DiscoveryApiWrapper(discoveryHost, discoveryPort)

  log.info("Starting the client...")
  (for (
    // Check for the presence of an AuthenticationApiWrapper
    (authenticationHost, authenticationPort) <- discoveryApiWrapper.discover(it.cwmp.services.authentication.Service.DISCOVERY_NAME);
    // Check for the presence of a RoomApiWrapper
    (roomsHost, roomsPort) <- discoveryApiWrapper.discover(it.cwmp.services.rooms.Service.DISCOVERY_NAME)
  ) yield {
    log.info(s"Initializing the API client actor...")
    val apiClientActor = system.actorOf(Props(ApiClientActor(
      AuthenticationApiWrapper(authenticationHost, authenticationPort),
      RoomsApiWrapper(roomsHost, roomsPort))), ApiClientActor.getClass.getName)
    log.info(s"Initializing the client controller actor...")
    system.actorOf(Props(ClientControllerActor(apiClientActor)), ClientControllerActor.getClass.getName)
    log.info("Client up and running!")
  }) andThen {
    case Failure(ex) => log.info("Error deploying RoomsService", ex)
  }
}
