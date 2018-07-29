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
    super.onErrorAlertReceived(title, message)
    onAlertReceived()
  }

  override protected def onInfoAlertReceived(title: String, message: String): Unit = {
    super.onInfoAlertReceived(title, message)
    onAlertReceived()
  }

  /**
    * When receiving an alert should enable buttons and hide loading
    */
  private def onAlertReceived(): Unit = { // TODO: check if those behaviours can be somehow made more clear
    fxController enableViewComponents()
    fxController hideLoading()
  }

  override protected def onHideGUI(): Unit = {
    super.onHideGUI()
    fxController hideLoading()
  }

}
