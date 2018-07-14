package it.cwmp.client.view.authentication

import akka.actor.{Actor, ActorRef}
import it.cwmp.client.controller.ClientControllerMessages
import it.cwmp.client.view.{AlertActor, FXAlerts}
import javafx.application.Platform
import javafx.embed.swing.JFXPanel

/**
  * Object that contains all the messages that this actor can receive.
  */
object AuthenticationViewMessages {

  case object InitController

  case object ShowGUI
}

object AuthenticationViewActor {
  def apply(): AuthenticationViewActor = new AuthenticationViewActor()
}

class AuthenticationViewActor extends Actor with AlertActor {

  var fxController: AuthenticationFXController = _
  var controllerActor: ActorRef = _


  override def preStart(): Unit = {
    super.preStart()

    //inizializzo il toolkit
    new JFXPanel
    Platform setImplicitExit false
    Platform runLater(() => {
      fxController = AuthenticationFXController(new AuthenticationFXStrategy {
        override def onSignIn(username: String, password: String): Unit =
          controllerActor ! ClientControllerMessages.AuthenticationPerformSignIn(username, password)

        override def onSignUp(username: String, password: String): Unit =
          controllerActor ! ClientControllerMessages.AuthenticationPerformSignUp(username, password)
      })
    })
  }

  override def receive: Receive = alertBehaviour orElse {
    case AuthenticationViewMessages.InitController => controllerActor = sender()
    case AuthenticationViewMessages.ShowGUI => Platform runLater(() => fxController showGUI())
  }
}
