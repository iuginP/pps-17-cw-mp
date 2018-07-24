package it.cwmp.services.authentication

import io.vertx.scala.core.Vertx
import it.cwmp.utils.Logging

/**
  * AuthenticationService entry-point
  */
object AuthenticationServiceMain extends App with Logging {

  Vertx.vertx().deployVerticle(new AuthenticationServiceVerticle)

  log.info("Deploying AuthenticationServiceVerticle... ")
}
