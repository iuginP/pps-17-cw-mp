package it.cwmp.client

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import it.cwmp.client.controller.actors.{ApiClientActor, ClientControllerActor}
import it.cwmp.services.wrapper.{AuthenticationApiWrapper, DiscoveryApiWrapper, RoomsApiWrapper}
import it.cwmp.services.{VertxInstance, discovery}
import it.cwmp.utils.{HostAndPortArguments, Logging}
import it.cwmp.view.TwoAddressesInput

import scala.util.Failure

/**
  * The client entry point
  */
object ClientMain extends App with VertxInstance with Logging {

  val APP_NAME = "CellWarsClient"
  val INITIAL_GUI_INSERTION_MESSAGE =
    """First pair -> DiscoveryService host-port
      |Second pair -> Your host IP, port will be ignored
    """.stripMargin

  private val COMMAND_LINE_ARGUMENTS_ERROR =
    """
      |Invalid number of arguments: I need
      |(1) the host address of the discovery service
      |(2) the port of the discovery service
    """.stripMargin

  try {
    val hostPortPairs = HostAndPortArguments(args, 1, COMMAND_LINE_ARGUMENTS_ERROR).pairs
    val discoveryService = hostPortPairs.head

    launch(discoveryService._1, discoveryService._2)
  } catch {
    case _: IllegalArgumentException =>
      TwoAddressesInput(APP_NAME, INITIAL_GUI_INSERTION_MESSAGE)(discoveryAndMyHostPortPairs => {
        val discoveryService = discoveryAndMyHostPortPairs._1
        val myHostIP = discoveryAndMyHostPortPairs._2._1

        launch(discoveryService._1, discoveryService._2)
      },
        _ => System.exit(0)
      )(firstDefaultPort = discovery.Service.DEFAULT_PORT.toString,
        secondDefaultPort = 0.toString)
  }

  /**
    * Launches client application
    *
    * @param discoveryHost the discovery to contact for services
    * @param discoveryPort the discovery port on which it listens
    */
  private def launch(discoveryHost: String, discoveryPort: String): Unit = {
    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=0").withFallback(ConfigFactory.load())
    val system = ActorSystem(APP_NAME, config)
    val discoveryApiWrapper: DiscoveryApiWrapper = DiscoveryApiWrapper(discoveryHost, discoveryPort.toInt)

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
      case Failure(ex) => log.info("Error discovering services", ex)
    }
  }
}
