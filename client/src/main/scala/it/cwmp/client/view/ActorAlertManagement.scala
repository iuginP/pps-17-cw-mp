package it.cwmp.client.view

import akka.actor.Actor.Receive
import it.cwmp.client.view.AlertMessages._

/**
  * A trait that gives autonomous management of alert messages;
  *
  * To use it you need to add "alertBehaviour" to your Actor receive
  *
  * @author Eugenio Pierfederici
  */
trait ActorAlertManagement {

  /**
    * @return the alerts controller
    */
  def fxAlertsController: FXAlerts

  /**
    * @return the behaviour that manages alert messages
    */
  protected def alertBehaviour: Receive = {
    case Info(title, message) =>
      fxAlertsController showInfo(title, message)
      onAlertReceived()
    case Error(title, message) =>
      fxAlertsController showError(title, message)
      onAlertReceived()
  }

  /**
    * It's called after receiving and showing the alert message
    */
  protected def onAlertReceived(): Unit = {}

}

/**
  * A collection of AlertMessages
  */
object AlertMessages {

  /**
    * Tells to show info message
    *
    * @param title   the title
    * @param message the message
    */
  case class Info(title: String, message: String)

  /**
    * Tells to show an error message
    *
    * @param title   the title
    * @param message the message
    */
  case class Error(title: String, message: String)

}
