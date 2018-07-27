package it.cwmp.client.view.room

import akka.actor.{Actor, ActorRef}
import it.cwmp.client.controller.ClientControllerMessages
import it.cwmp.client.view.AlertActor
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

  case object ShowToken

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
class RoomViewActor extends Actor with AlertActor {
  /**
    * roomFXController il controller che gestisce la view della lobby delle stanze
    */
  var fxController: RoomFXController = _
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
    //inizializzo il toolkit
    new JFXPanel
    Platform setImplicitExit false
    Platform runLater (() => {
      fxController = RoomFXController(new RoomFXStrategy {
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
    case RoomViewMessages.ShowGUI => Platform runLater (() => fxController.showGUI())
    case RoomViewMessages.HideGUI => Platform runLater (() => {
      fxController.hideGUI()
      fxController hideLoadingDialog()
    })
  }

  override protected def onAlertReceived(): Unit = {
    fxController enableButtons()
    fxController hideLoadingDialog()
  }

  override protected def onTokenReceived(title: String, message: String): Unit = {
    fxController hideLoadingDialog()
    fxController showTokenDialog(title, message)
  }
}
