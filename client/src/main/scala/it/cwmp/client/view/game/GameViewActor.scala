package it.cwmp.client.view.game

import akka.actor.{Actor, ActorRef, Cancellable}
import it.cwmp.client.controller.game.GameEngine
import it.cwmp.client.model.DistributedStateMessages
import it.cwmp.client.model.game.impl._
import it.cwmp.client.view.game.GameViewActor._
import it.cwmp.client.view.game.model.CellView
import it.cwmp.utils.Logging

import scala.concurrent.duration._

/**
  * @author contributor Enrico Siboni
  */
class GameViewActor(parentActor: ActorRef) extends Actor with Logging {

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
      //  log.info(s"World to paint: Characters=${tempWorld.characters} Attacks=${tempWorld.attacks} Instant=${tempWorld.instant}")
      gameFX.updateWorld(tempWorld)

      // is that to heavy computation here ???
      tempWorld = GameEngine(tempWorld, java.time.Duration.ofMillis(FRAME_RATE.toMillis))

    case AddAttack(from, to) =>
      log.info(s"AddAttack from:$from to:$to")
      val worldCharacters = tempWorld.characters
      val fromCell = findCellNearTo(from, worldCharacters)
      val toCell = findCellNearTo(to, worldCharacters)
      log.debug(s"Attack detected from $fromCell to $toCell")
      (fromCell, toCell) match {
        case (Some(attacker), Some(attacked)) =>
          tempWorld = tempWorld ++ Tentacle(attacker, attacked, tempWorld.instant)
          parentActor ! DistributedStateMessages.UpdateWorld(tempWorld)
        case tmp@_ => log.debug(s"No attack detected... $tmp")
      }


    case RemoveAttack(pointOnAttackView) =>
      log.info(s"RemoveAttack pointOnView:$pointOnAttackView")
    // TODO: convert point on attack view to the corresponding tentacle, calculating the distance of this point
    // TODO: from the straight line passing through the tentacle "from" and "to" points
  }
}

/**
  * Companion object, containing actor messages
  */
object GameViewActor {
  def apply(parentActor: ActorRef): GameViewActor = new GameViewActor(parentActor)

  case object ShowGUI

  case object HideGUI

  case class NewWorld(world: CellWorld)

  case object UpdateLocalWorld


  /**
    * A message stating that an attack has been launched from one point to another
    *
    * @param from the point from which attack is starting
    * @param to   the point to which the attack is going
    */
  case class AddAttack(from: Point, to: Point)

  /**
    * A message stating that an attack has been removed
    *
    * @param pointOnAttackView the point clicked by the player to remove the attack
    */
  case class RemoveAttack(pointOnAttackView: Point)

  /**
    * A method to find a cell near to a clicked point on view, according to actual cell sizing
    *
    * @param clickedPoint the clicked point on view
    * @param cells        the collection of cells on screen
    * @return optionally the cell near the clicked point
    */
  private def findCellNearTo(clickedPoint: Point, cells: Seq[Cell]): Option[Cell] =
    cells.find(cell => GeometricUtils.isWithinCircumference(clickedPoint, cell.position, CellView.sizingStrategy(cell)))
}
