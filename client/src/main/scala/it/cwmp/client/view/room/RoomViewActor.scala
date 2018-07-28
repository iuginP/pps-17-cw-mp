package it.cwmp.client.view.room

import akka.actor.{Actor, ActorRef}
import it.cwmp.client.controller.ClientControllerMessages
import it.cwmp.client.view.ActorAlertManagement
import javafx.application.Platform
import javafx.embed.swing.JFXPanel

/**
  * Questo oggetto contiene tutti i messaggi che questo attore può ricevere.
  */
object RoomViewMessages {

  /**
    * Questo messaggio rappresenta l'inizializzazione del controller che verrà poi utilizzato per le rispote che verrano inviate al mittente.
    * Quando ricevuto, inizializzo il controller.
    */
  case object InitController

  /**
    * Questo messaggio rappresenta la visualizzazione dell'interfaccia grafica.
    * Quando ricevuto, viene mostrata all'utente l'interfaccia grafica.
    */
  case object ShowGUI

  /**
    * Questo messaggio rappresenta la chiusura dell'interfaccia grafica.
    * Quando ricevuto, viene nascosta all'utente l'interfaccia grafica di selezione delle stanze.
    */
  case object HideGUI

  /**
    * Questo messaggio serve per visualizzare a schermo il token di creazione della stanza privata
    *
    * @param title   il tiolo da visualizzare nel dialog
    * @param message il token vero e proprio
    */
  case class ShowToken(title: String, message: String)

}

object RoomViewActor {
  def apply(): RoomViewActor = new RoomViewActor()
}

/**
  * Questa classe rappresenta l'attore incaricato di visualizzare
  * l'interfaccia grafica della lobby di selezione delle stanze.
  *
  * @author Davide Borficchia
  */
class RoomViewActor extends Actor with ActorAlertManagement {
  /**
    * roomFXController il controller che gestisce la view della lobby delle stanze
    */
  var fxAlertsController: RoomFXController = _
  /**
    * Questo è l'attore che ci invia i messaggi e quello al quale dobbiamo rispondere
    */
  var controllerActor: ActorRef = _

  /**
    * Questo metodo viene invocato alla creazione dell'attore. Non va mai chiamato direttamente!
    * Si occupa di creare il controller/view di javaFX per gestire il layout grafico delle stanze.
    */
  override def preStart(): Unit = {
    super.preStart()

    new JFXPanel // initializes JavaFX
    Platform setImplicitExit false
    Platform runLater (() => {
      fxAlertsController = RoomFXController(new RoomFXStrategy {
        override def onCreate(name: String, nPlayer: Int): Unit =
          controllerActor ! ClientControllerMessages.RoomCreatePrivate(name, nPlayer)

        override def onEnterPrivate(idRoom: String): Unit =
          controllerActor ! ClientControllerMessages.RoomEnterPrivate(idRoom)

        override def onEnterPublic(nPlayer: Int): Unit =
          controllerActor ! ClientControllerMessages.RoomEnterPublic(nPlayer)
      })
    })
  }

  /**
    * Questo metodo viene chiamato in automatico dall'esterno ogni volta che viene ricevuto un messaggio.
    * Non va mai chiamato direttamente!
    * I messaggi che questo attore è ingrado di ricevere sono raggruppati in [[RoomViewMessages]]
    */
  override def receive: Receive = alertBehaviour orElse {
    case RoomViewMessages.InitController => controllerActor = sender()
    case RoomViewMessages.ShowGUI => Platform runLater (() => fxAlertsController.showGUI())
    case RoomViewMessages.HideGUI => Platform runLater (() => {
      fxAlertsController.hideGUI()
      fxAlertsController hideLoading()
    })
    case RoomViewMessages.ShowToken(title, roomToken) => Platform runLater (() => {
      // TODO: remove "title" from this message, view should be able to decide what to write
      onAlertReceived()
      fxAlertsController showTokenDialog roomToken // TODO: make possible to close dialogs whit X
    })
  }

  override protected def onErrorAlertReceived(title: String, message: String): Unit = {
    super.onErrorAlertReceived(title, message)
    onAlertReceived() // TODO: duplicated code in AuthenticationActor
  }

  override protected def onInfoAlertReceived(title: String, message: String): Unit = {
    super.onInfoAlertReceived(title, message)
    onAlertReceived()
  }

  /**
    * When receiving an alert should enable buttons and hide loading
    */
  private def onAlertReceived(): Unit = {
    fxAlertsController enableViewComponents()
    fxAlertsController hideLoading()
  }
}
