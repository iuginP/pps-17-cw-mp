package it.cwmp.client.controller

import io.vertx.scala.core.Vertx
import it.cwmp.authentication.AuthenticationService

abstract class AuthenticationController extends ViewController {
  protected val auth = AuthenticationService(Vertx.vertx())
}
