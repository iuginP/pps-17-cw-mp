package it.cwmp.client.view.game

import akka.actor.Actor
import it.cwmp.client.model.game.World

object GameViewActor {
  def apply(): GameViewActor = new GameViewActor

  case object ShowGUI

  case object HideGUI

  case class UpdateWorld(status: World)

}

import it.cwmp.client.view.game.GameViewActor._

class GameViewActor extends Actor {

  val gameFX: GameFX = GameFX()
  var isHidden = true

  override def receive: Receive = {
    case ShowGUI =>
      if (isHidden) {
        isHidden = false
        gameFX.start("GIOCO", 512)
      }
    case HideGUI =>
      if (!isHidden) {
        isHidden = true
        gameFX.close()
      }
    case UpdateWorld(world) =>
      gameFX.updateWorld(world)
  }
}
