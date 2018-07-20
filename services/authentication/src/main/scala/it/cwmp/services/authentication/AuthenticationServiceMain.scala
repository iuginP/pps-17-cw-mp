package it.cwmp.services.authentication

import io.vertx.scala.core.Vertx

/**
  * Hello class for server
  */
object AuthenticationServiceMain extends App {

  Vertx.vertx().deployVerticle(new AuthenticationServiceVerticle)

  println("Deploying AuthenticationServiceVerticle... ") // TODO replace with logger logging
}
