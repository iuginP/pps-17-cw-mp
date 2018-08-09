package it.cwmp.services.discovery

import it.cwmp.utils.{Logging, VertxInstance}

import scala.util.{Failure, Success}

/**
  * DiscoveryService entry-point
  */
object DiscoveryServiceMain extends App with VertxInstance with Logging {

  log.info("Deploying DiscoveryService... ")
  vertx.deployVerticleFuture(DiscoveryServiceVerticle())
    .andThen {
      case Success(_) => log.info("DiscoveryService up and running!")
      case Failure(ex) => log.info("Error deploying DiscoveryService", ex)
    }
}
