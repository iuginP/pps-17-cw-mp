package it.cwmp.client

import akka.actor.{ActorSystem, Props}
import it.cwmp.client.view.room.{RoomViewActor, RoomViewMessages}

object RoomManagerMain extends App {
  val system = ActorSystem("test")
  val actor = system.actorOf(Props[RoomViewActor], "roomView")
  actor ! RoomViewMessages.ShowGUI
}
