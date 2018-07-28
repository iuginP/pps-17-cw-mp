package it.cwmp.client.controller

import akka.actor.Actor.Receive
import it.cwmp.client.controller.AlertMessages._
import it.cwmp.client.view.FXAlertsController

/**
  * A trait that gives autonomous management of alert messages;
  *
  * To use it you need to add "alertBehaviour" to your Actor receive
  *
  * @author Eugenio Pierfederici
  * @author contributor Enrico Siboni
  */
trait ActorAlertManagement {

  /**
    * @return the alerts controller
    */
  def fxController: FXAlertsController

  /**
    * @return the behaviour that manages alert messages
    */
  protected def alertBehaviour: Receive = {
    case Info(title, message) => onInfoAlertReceived(title, message)
    case Error(title, message) => onErrorAlertReceived(title, message)
  }

  /**
    * Called when a info alert is received
    *
    * @param title   the title of the info
    * @param message the message of the info
    */
  protected def onInfoAlertReceived(title: String, message: String): Unit =
    fxController showInfo(title, message)

  /**
    * Called when an error alert is received
    *
    * @param title   the title of the error
    * @param message the message of the error
    */
  protected def onErrorAlertReceived(title: String, message: String): Unit =
    fxController showError(title, message)
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
