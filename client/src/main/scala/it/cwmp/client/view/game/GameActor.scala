package it.cwmp.client.view.game

import akka.actor.Actor
import it.cwmp.client.model.game.World

object GameActor {
  def apply(): GameActor = new GameActor

  case object ShowGUI
  case object HideGUI
  case class UpdateWorld(status: World)
}

import it.cwmp.client.view.game.GameActor._
class GameActor extends Actor {

  val gameFX: GameFX = GameFX()

  override def receive: Receive = {
    case ShowGUI =>
      gameFX.start("GIOCO", 512)
    case HideGUI =>
      gameFX.close()
    case UpdateWorld(world) =>
      gameFX.updateWorld(world)
  }
}
