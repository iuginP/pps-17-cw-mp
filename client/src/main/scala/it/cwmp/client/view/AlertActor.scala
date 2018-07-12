package it.cwmp.client.view

import akka.actor.Actor.Receive

object AlertMessages {

  case class Info(title: String, message: String)
  case class Error(title: String,message: String)
}

trait AlertActor {

  def fxController: FXAlerts

  import AlertMessages._
  protected def alertBehaviour: Receive = {
    case Info(title, message) => fxController showInfo(title, message)
    case Error(title, message) => fxController showError(title, message)
  }
}
