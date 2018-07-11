package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.cwmp.client.view.authentication.{AuthenticationViewActor, AuthenticationViewMessages}
import it.cwmp.client.view.room.{RoomViewActor, RoomViewMessages}

/**
  * Questo oggetto contiene tutti i messaggi che questo attore può ricevere.
  */
object ClientControllerMessages {

  case class AuthenticationPerformSignIn(username: String, password: String)

  case class AuthenticationPerformSignUp(username: String, password: String)

  /**
    * Questo messaggio rappresenta la visualizzazione dell'interfaccia grafica per la gestione delle lobby delle stanze.
    * Quando lo ricevuto, viene mostrata all'utente l'interfaccia grafica.
    *
    * @param name è il nome della stanza da creare
    * @param nPlayer è il numero dei giocatori che potranno entrare nella stanza
    */
  case class RoomCreatePrivate(name: String, nPlayer: Int)
}

object ClientControllerActor {
  def apply(system: ActorSystem): ClientControllerActor = new ClientControllerActor(system)
}

/**
  * Questa classe rappresenta l'attore del controller del client che ha il compito
  * di fare da tramite tra le view e i model.
  *
  * @author Davide Borficchia
  */
class ClientControllerActor(system: ActorSystem) extends Actor{

  var authenticationViewActor: ActorRef = _
  var roomViewActor: ActorRef = _

  override def preStart(): Unit = {
    super.preStart()

    authenticationViewActor = system.actorOf(Props[AuthenticationViewActor], "authenticationView")
    authenticationViewActor ! AuthenticationViewMessages.InitController
    // TODO debug, remove before release
    authenticationViewActor ! AuthenticationViewMessages.ShowGUI

    /*// Initialize all actors
    roomViewActor = system.actorOf(Props[RoomViewActor], "roomView")
    roomViewActor ! RoomViewMessages.InitController
    // TODO debug, remove before release
    roomViewActor ! RoomViewMessages.ShowGUI*/
  }

  def receive = authenticationManagerBehaviour
  //possibilità di aggiungere altri behavior
  //.orElse[Any, Unit](receiveAddItem)

  def becomeAuthenticationManager(): Unit = {
    context.become(authenticationManagerBehaviour)
  }

  def becomeRoomsManager(): Unit = {
    context.become(roomManagerBehaviour)
  }


  def authenticationManagerBehaviour: Receive = {
    case ClientControllerMessages.AuthenticationPerformSignIn(username, password) => println(s"Perform sign-in with: $username, $password") // TODO crea stanza
    case ClientControllerMessages.AuthenticationPerformSignUp(username, password) => println(s"Perform sign-up with: $username, $password") // TODO crea stanza
  }

  /**
    * Questo metodo rappresenta il behavior da avere quando si sta gestendo la lobby delle stanze.
    * I messaggi che questo attore, in questo behavoir, è ingrado di ricevere sono raggruppati in [[ClientControllerMessages]]
    *
    */
  def roomManagerBehaviour: Receive = {
    case ClientControllerMessages.RoomCreatePrivate(name, nPlayer) => println("prova", name, nPlayer) // TODO crea stanza
  }



}
