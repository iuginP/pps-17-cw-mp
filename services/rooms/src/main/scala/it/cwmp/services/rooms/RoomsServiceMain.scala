package it.cwmp.services.rooms

import it.cwmp.services.wrapper.{AuthenticationApiWrapper, DiscoveryApiWrapper, RoomReceiverApiWrapper}
import it.cwmp.utils.{Logging, ServiceArguments, VertxInstance}

import scala.util.Failure

/**
  * Object that implements the Rooms micro-service entry-point
  *
  * @author Enrico Siboni
  */
object RoomsServiceMain extends App with VertxInstance with Logging {

  val arguments = ServiceArguments(args)
  // scalastyle:off import.grouping
  import arguments._
  // scalastyle:on import.grouping

  private val discoveryApiWrapper: DiscoveryApiWrapper = DiscoveryApiWrapper(discoveryHost, discoveryPort)

  log.info("Deploying RoomService...")
  for (
    // Check for the presence of an AuthenticationApiWrapper
    (host, port) <- discoveryApiWrapper.discover(it.cwmp.services.authentication.Service.DISCOVERY_NAME);
    _ <- vertx.deployVerticleFuture(RoomsServiceVerticle(currentPort, AuthenticationApiWrapper(host, port), RoomReceiverApiWrapper()))
  ) yield {
    log.info("RoomsService up and running!")
    discoveryApiWrapper.publish(Service.DISCOVERY_NAME, currentHost, currentPort)
  } andThen {
    case Failure(ex) => log.info("Error deploying RoomsService", ex)
  }
}
