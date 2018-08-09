package it.cwmp.services.rooms

import it.cwmp.services.wrapper.{AuthenticationApiWrapper, DiscoveryApiWrapper, RoomReceiverApiWrapper}
import it.cwmp.services.{ServiceLauncher, VertxInstance, discovery, rooms}
import it.cwmp.utils.{HostAndPortArguments, Logging}
import it.cwmp.view.TwoAddressesInput

import scala.util.Failure

/**
  * Object that implements the Rooms micro-service entry-point
  *
  * @author Enrico Siboni
  */
object RoomsServiceMain extends App with VertxInstance with Logging with ServiceLauncher {

  private val SERVICE_NAME = "Rooms Service"

  override def launch(discoveryHost: String, discoveryPort: String, myHost: String, myPort: String): Unit = {
    val discoveryApiWrapper: DiscoveryApiWrapper = DiscoveryApiWrapper(discoveryHost, discoveryPort.toInt)

    log.info("Deploying RoomService...")
    for (
      // Check for the presence of an AuthenticationApiWrapper
      (host, port) <- discoveryApiWrapper.discover(it.cwmp.services.authentication.Service.DISCOVERY_NAME);
      _ <- vertx.deployVerticleFuture(RoomsServiceVerticle(myPort.toInt, AuthenticationApiWrapper(host, port), RoomReceiverApiWrapper()))
    ) yield {
      log.info("RoomsService up and running!")
      discoveryApiWrapper.publish(Service.DISCOVERY_NAME, myHost, myPort.toInt)
    } andThen {
      case Failure(ex) => log.info("Error deploying RoomsService", ex)
    }
  }


  try {
    val hostPortPairs = HostAndPortArguments(args, 2, ServiceLauncher.COMMAND_LINE_ARGUMENTS_ERROR).pairs
    val discoveryService = hostPortPairs.head
    val myService = hostPortPairs(1)

    launch(discoveryService._1, discoveryService._2, myService._1, myService._2)
  } catch {
    case _: IllegalArgumentException =>
      TwoAddressesInput(SERVICE_NAME, ServiceLauncher.GUI_INSERTION_MESSAGE, discoveryAndMyHostPortPairs => {
        val discoveryService = discoveryAndMyHostPortPairs._1
        val myService = discoveryAndMyHostPortPairs._2

        launch(discoveryService._1, discoveryService._2, myService._1, myService._2)
      })(firstDefaultPort = discovery.ServerParameters.DEFAULT_PORT.toString,
        secondDefaultPort = rooms.ServerParameters.DEFAULT_PORT.toString)
  }
}
