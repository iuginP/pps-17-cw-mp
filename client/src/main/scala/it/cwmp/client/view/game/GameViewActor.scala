package it.cwmp.client.view.game

import akka.actor.{Actor, ActorLogging, Cancellable}
import it.cwmp.client.controller.game.GameEngine
import it.cwmp.client.model.game.impl.CellWorld

import scala.concurrent.duration._

object GameViewActor {
  def apply(): GameViewActor = new GameViewActor

  case object ShowGUI

  case object HideGUI

  case class NewWorld(world: CellWorld)

  case object UpdateLocalWorld

}

import it.cwmp.client.view.game.GameViewActor._

/**
  * @author contributor Enrico Siboni
  */
class GameViewActor extends Actor with ActorLogging {

  private val gameFX: GameFX = GameFX()
  private val FRAME_RATE: FiniteDuration = 50.millis

  private var isHidden = true
  private var updatingSchedule: Cancellable = _
  private var tempWorld: CellWorld = _

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
    case NewWorld(world) =>
      if (updatingSchedule != null) updatingSchedule.cancel()
      tempWorld = world
      updatingSchedule = context.system.scheduler
        .schedule(0.millis,
          FRAME_RATE,
          self,
          UpdateLocalWorld)(context.dispatcher)

    case UpdateLocalWorld =>
      log.info(s"World to paint: Characters=${tempWorld.characters} Attacks=${tempWorld.attacks} Instant=${tempWorld.instant}")
      gameFX.updateWorld(tempWorld)

      // is that to heavy computation here ???
      tempWorld = GameEngine(tempWorld, java.time.Duration.ofMillis(FRAME_RATE.toMillis))
  }
}
