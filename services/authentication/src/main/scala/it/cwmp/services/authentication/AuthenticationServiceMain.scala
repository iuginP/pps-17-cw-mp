package it.cwmp.services.authentication

import it.cwmp.utils.{Logging, VertxInstance}

/**
  * AuthenticationService entry-point
  */
object AuthenticationServiceMain extends App with VertxInstance with Logging {

  vertx.deployVerticle(new AuthenticationServiceVerticle)

  log.info("Deploying AuthenticationServiceVerticle... ")
}
