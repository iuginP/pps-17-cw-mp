package it.cwmp.client

import akka.actor.{ActorSystem, Props}
import it.cwmp.client.controller.ClientControllerActor

object AuthenticationMain extends App {
  val system = ActorSystem("test")

  val clientControllerActor = system.actorOf(Props(classOf[ClientControllerActor], system), "clientController")
}
