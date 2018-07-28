package it.cwmp.client.view

import akka.actor.Actor.Receive
import it.cwmp.client.view.AlertMessages._

object AlertMessages {

  /**
    * Questo messagio serve per mostrare un messaggio all'utente di tipo informativo.
    *
    * @param title   contiene il titolo del messaggio
    * @param message contiene il corpo del messaggio da visualizzare
    */
  case class Info(title: String, message: String, onClose: Option[() => Unit] = None)

  /**
    * Questo messagio serve per mostrare un messaggio di errore all'utente.
    *
    * @param title   contiene il titolo del messaggio
    * @param message contiene il corpo del messaggio da visualizzare
    */
  case class Error(title: String, message: String, onClose: Option[() => Unit] = None)

  /**
    * Questo messagio serve per mostrare un messaggio di contenente il token della stanza privata appena creata all'utente.
    *
    * @param title   contiene il titolo del messaggio
    * @param message contiene il corpo del messaggio da visualizzare
    */
  case class Token(title: String, message: String)

}

/**
  * A trait that gives autonomous management of alert messages;
  *
  * To use it you need to add "alertBehaviour" to your Actor receive
  *
  * @author Eugenio Pierfederici
  */
trait AlertActor {

  def fxController: FXAlerts

  /**
    * Il behaviour che si occupa di restare in ascolto per i messaggi specificati in [[AlertMessages]].
    */

  protected def alertBehaviour: Receive = {
    case Info(title, message, onClose) =>
      fxController showInfo(title, message)
      onAlertReceived()
    case Error(title, message, onClose) =>
      fxController showError(title, message)
      onAlertReceived()
  }

  /**
    * metodo per aggiungere un comportamento quando si riceve un alert
    */
  protected def onAlertReceived(): Unit = {}

}
