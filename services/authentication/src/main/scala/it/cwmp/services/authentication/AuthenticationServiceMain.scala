package it.cwmp.services.authentication

import io.vertx.scala.core.Vertx
import it.cwmp.utils.Logging

/**
  * Hello class for server
  */
object AuthenticationServiceMain extends App with Logging {

  Vertx.vertx().deployVerticle(new AuthenticationServiceVerticle)

  log.info("Deploying AuthenticationServiceVerticle... ")
}
