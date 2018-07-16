package it.cwmp.client.view

import akka.actor.Actor.Receive

object AlertMessages {

  /**
    * Questo messagio serve per mostrare un messaggio all'utente di tipo informativo.
    *
    * @param title contiene il titolo del messaggio
    * @param message contiene il corpo del messaggio da visualizzare
    */
  case class Info(title: String, message: String, onClose: Option[() => Unit] = None)

  /**
    * Questo messagio serve per mostrare un messaggio di errore all'utente.
    *
    * @param title contiene il titolo del messaggio
    * @param message contiene il corpo del messaggio da visualizzare
    */
  case class Error(title: String, message: String, onClose: Option[() => Unit] = None)
}

/**
  *
  * Trait da estendere ogni volta che si vuole avere in automatico la gestione di dei messaggi informativi o di errore.
  * Per usarlo va aggiunto il behaviour: alertBehaviour al comportamento della receive.
  *
  * @author Eugenio Pierfederici
  */
trait AlertActor {

  def fxController: FXAlerts

  /**
    * Il behaviour che si occupa di restare in ascolto per i messaggi specificati in [[AlertMessages]].
    */
  import AlertMessages._
  protected def alertBehaviour: Receive = {
    case Info(title, message, onClose) => fxController showInfo(title, message, onClose)
    case Error(title, message, onClose) => fxController showError(title, message, onClose)
  }
}
