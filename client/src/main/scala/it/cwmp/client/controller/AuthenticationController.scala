package it.cwmp.client.controller

import it.cwmp.authentication.AuthenticationService

abstract class AuthenticationController extends ViewController {
  protected val auth = AuthenticationService()
}
