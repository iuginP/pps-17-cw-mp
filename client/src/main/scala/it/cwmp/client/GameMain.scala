package it.cwmp.client

import java.time.{Duration, Instant}

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import it.cwmp.client.model.game.impl.{Cell, CellWorld, Point, Tentacle}
import it.cwmp.client.view.game.GameViewActor
import it.cwmp.model.User

object GameMain extends App {
  val APP_NAME = "ClientApp"

  private val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=0").withFallback(ConfigFactory.load())

  val system = ActorSystem(APP_NAME, config)

  val gameActor = system.actorOf(Props(classOf[GameViewActor]), GameViewActor.getClass.getName)

  import GameViewActor._

  gameActor ! ShowGUI
  gameActor ! UpdateWorld(debugWorld)

  def debugWorld: CellWorld = {
    val cells =
      Cell(User("Winner"), Point(20, 20), 20) ::
        Cell(User("Mantis"), Point(90, 400), 40) ::
        Cell(User("Candle"), Point(200, 150), 200) ::
        Nil
    CellWorld(Instant.now().minus(Duration.ofSeconds(10)), cells,
      Tentacle(cells(0), cells(1), Instant.now()) ::
        Tentacle(cells(1), cells(2), Instant.now()) ::
        Tentacle(cells(2), cells(0), Instant.now()) ::
        Nil)
  }
}
