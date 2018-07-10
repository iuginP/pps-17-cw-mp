package it.cwmp.client.view.room

import akka.actor.{Actor, ActorRef}
import it.cwmp.client.controller.ClientControllerMessages
import javafx.application.Platform
import javafx.embed.swing.JFXPanel

/**
  * Questo oggetto contiene tutti i messaggi che questo attore può ricevere.
  */
object RoomViewMessages {

  case object InitController
  /**
    * Questo messaggio rappresenta la visualizzazione dell'interfaccia grafica.
    * Quando ricevuto, viene mostrata all'utente l'interfaccia grafica.
    */
  case object ShowGUI
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
class RoomViewActor extends Actor{

  var roomFXController: RoomFXController = _
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
    Platform runLater(() => {
      roomFXController = RoomFXController((name: String, nPlayer: Int) =>
        controllerActor ! ClientControllerMessages.RoomCreatePrivate(name, nPlayer))
    })
  }

  /**
    * Questo metodo viene chiamato in automatico dall'esterno ogni volta che viene ricevuto un messaggio.
    * Non va mai chiamato direttamente!
    * I messaggi che questo attore è ingrado di ricevere sono raggruppati in [[RoomViewMessages]]
    */
  override def receive: Receive = {
    case RoomViewMessages.InitController => controllerActor = sender()
    case RoomViewMessages.ShowGUI => Platform runLater(() => roomFXController.showGUI())
  }
}
