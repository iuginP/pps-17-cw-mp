package it.cwmp.client.view

import akka.actor.{Actor, ActorRef}
import it.cwmp.client.controller.messages.Initialize
import it.cwmp.client.controller.{ActorAlertManagement, ActorViewVisibilityManagement}
import javafx.application.Platform
import javafx.embed.swing.JFXPanel

/**
  * A base class representing a Service View actor with JavaFX underlying
  *
  * @author Enrico Siboni
  */
abstract class FXServiceViewActor extends Actor with ActorAlertManagement with ActorViewVisibilityManagement {

  protected def fxController: FXViewController with FXAlertsController with FXInputViewController

  protected var controllerActor: ActorRef = _

  override def preStart(): Unit = {
    super.preStart()
    new JFXPanel // initializes JavaFX
    Platform setImplicitExit false
  }

  override def receive: Receive = alertBehaviour orElse visibilityBehaviour orElse {
    case Initialize => controllerActor = sender()
  }

  override protected def onErrorAlertReceived(title: String, message: String): Unit = {
    onServiceResponseReceived()
    super.onErrorAlertReceived(title, message)
  }

  override protected def onInfoAlertReceived(title: String, message: String): Unit = {
    onServiceResponseReceived()
  }

  /**
    * When receiving a response from contacted service should enable buttons and hide loading
    */
  protected def onServiceResponseReceived(): Unit = {
    fxController enableViewComponents()
    fxController hideLoading()
  }

}
