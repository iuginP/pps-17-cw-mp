package it.cwmp.client

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import it.cwmp.client.controller.ClientControllerActor

/**
  * The client entry point
  */
object ClientMain extends App {

  // Terminal arguments check
  require(args.length == 2,
    """
      Invalid number of arguments: I need
      (1) the host address of the discovery service
      (2) the port of the discovery service
    """)

  val discovery_host: String = args(0)
  val discovery_port: Int = args(1) toInt

  val APP_NAME = "CellWarsClient"

  private val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=0").withFallback(ConfigFactory.load())
  val system = ActorSystem(APP_NAME, config)
  val clientControllerActor = system.actorOf(Props(classOf[ClientControllerActor], discovery_host, discovery_port), ClientControllerActor.getClass.getName)
}
