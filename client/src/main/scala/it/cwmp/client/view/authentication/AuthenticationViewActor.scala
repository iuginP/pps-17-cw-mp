package it.cwmp.client.view.authentication

import it.cwmp.client.controller.messages.AuthenticationRequests.{LogIn, SignUp}
import it.cwmp.client.view.FXViewActor

/**
  * Actor assigned to the management of the display of the authentication screen and of the events generated by it.
  *
  * @author Elia Di Pasquale
  * @author contributor Enrico Siboni
  */
case class AuthenticationViewActor() extends FXViewActor {

  protected var fxController: AuthenticationFXController = _

  override def preStart(): Unit = {
    super.preStart()
    runOnUIThread(() =>
      fxController = AuthenticationFXController(new AuthenticationStrategy {
        override def performLogIn(username: String, password: String): Unit =
          controllerActor ! LogIn(username, password)

        override def performPasswordCheck(password: String, confirmPassword: String): Boolean =
          password == confirmPassword

        override def performSignUp(username: String, password: String): Unit =
          controllerActor ! SignUp(username, password)
      }))
  }
}