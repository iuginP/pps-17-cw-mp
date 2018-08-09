package it.cwmp.client

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import it.cwmp.client.controller.ClientControllerActor
import it.cwmp.services.discovery
import it.cwmp.utils.HostAndPortArguments
import it.cwmp.view.OneAddressInput

/**
  * The client entry point
  */
object ClientMain extends App {

  val APP_NAME = "CellWarsClient"

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
      OneAddressInput(APP_NAME, "You should insert DiscoveryService host-port", discoveryServiceHostPortPair => {
        launch(discoveryServiceHostPortPair._1, discoveryServiceHostPortPair._2)
      })(defaultPort = discovery.Service.DEFAULT_PORT.toString)
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

    system.actorOf(
      Props(classOf[ClientControllerActor], discoveryHost, discoveryPort.toInt),
      ClientControllerActor.getClass.getName)
  }
}
