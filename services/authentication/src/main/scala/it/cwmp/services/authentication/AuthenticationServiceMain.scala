package it.cwmp.services.authentication

import it.cwmp.services.wrapper.DiscoveryApiWrapper
import it.cwmp.services.{ServiceLauncher, VertxInstance, _}
import it.cwmp.utils.{HostAndPortArguments, Logging}
import it.cwmp.view.TwoAddressesInput

import scala.util.{Failure, Success}

/**
  * AuthenticationService entry-point
  */
object AuthenticationServiceMain extends App with VertxInstance with Logging with ServiceLauncher {

  try {
    val hostPortPairs = HostAndPortArguments(args, 2, ServiceLauncher.COMMAND_LINE_ARGUMENTS_ERROR).pairs
    val discoveryService = hostPortPairs.head
    val myService = hostPortPairs(1)

    launch(discoveryService._1, discoveryService._2, myService._1, myService._2)
  } catch {
    case _: IllegalArgumentException =>
      TwoAddressesInput(Service.COMMON_NAME, ServiceLauncher.GUI_INSERTION_MESSAGE)(discoveryAndMyHostPortPairs => {
        val discoveryService = discoveryAndMyHostPortPairs._1
        val myService = discoveryAndMyHostPortPairs._2

        launch(discoveryService._1, discoveryService._2, myService._1, myService._2)
      },
        _ => System.exit(0)
      )(firstDefaultPort = discovery.Service.DEFAULT_PORT.toString,
        secondDefaultPort = Service.DEFAULT_PORT.toString)
  }

  override def launch(discoveryHost: String, discoveryPort: String, myHost: String, myPort: String): Unit = {
    log.info("Deploying AuthenticationService... ")
    vertx.deployVerticleFuture(AuthenticationServiceVerticle(myPort.toInt))
      .andThen {
        case Success(_) =>
          log.info("AuthenticationService up and running!")
          DiscoveryApiWrapper(discoveryHost, discoveryPort.toInt)
            .publish(Service.DISCOVERY_NAME, myHost, myPort.toInt)
        case Failure(ex) => log.info("Error deploying AuthenticationService", ex)
      }
  }
}
