package it.cwmp.services.authentication

import it.cwmp.services.VertxInstance
import it.cwmp.services.wrapper.DiscoveryApiWrapper
import it.cwmp.utils.{Logging, ServiceArguments}

import scala.util.{Failure, Success}

/**
  * AuthenticationService entry-point
  */
object AuthenticationServiceMain extends App with VertxInstance with Logging {

  val arguments = ServiceArguments(args)
  // scalastyle:off import.grouping
  import arguments._
  // scalastyle:on import.grouping

  // Executing the app
  log.info("Deploying AuthenticationService... ")
  vertx.deployVerticleFuture(AuthenticationServiceVerticle(currentPort))
    .andThen {
      case Success(_) =>
        log.info("AuthenticationService up and running!")
        DiscoveryApiWrapper(discoveryHost, discoveryPort)
          .publish(Service.DISCOVERY_NAME, currentHost, currentPort)
      case Failure(ex) => log.info("Error deploying AuthenticationService", ex)
    }
}
