package it.cwmp.services.authentication

import it.cwmp.utils.{Logging, VertxInstance}

import scala.util.{Failure, Success}

/**
  * AuthenticationService entry-point
  */
object AuthenticationServiceMain extends App with VertxInstance with Logging {

  log.info("Deploying AuthenticationService... ")
  vertx.deployVerticleFuture(new AuthenticationServiceVerticle)
    .andThen {
      case Success(_) => log.info("AuthenticationService up and running!")
      case Failure(ex) => log.info("Error deploying AuthenticationService", ex)
    }
}
