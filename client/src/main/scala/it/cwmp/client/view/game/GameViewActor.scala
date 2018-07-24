package it.cwmp.client.view.game

import akka.actor.{Actor, ActorRef, Cancellable}
import it.cwmp.client.controller.game.GameEngine
import it.cwmp.client.model.DistributedStateMessages
import it.cwmp.client.model.game.impl._
import it.cwmp.client.view.game.GameViewActor._
import it.cwmp.client.view.game.model.{CellView, TentacleView}
import it.cwmp.utils.Logging

import scala.concurrent.duration._

/**
  * The actor that deals with Game View
  *
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
      (fromCell, toCell) match {
        case (Some(attacker), Some(attacked)) =>
          log.debug(s"Adding attack from $attacker to $attacked ...")
          tempWorld = tempWorld ++ Tentacle(attacker, attacked, tempWorld.instant)
          parentActor ! DistributedStateMessages.UpdateWorld(tempWorld)
        case tmp@_ => log.debug(s"No cells detected $tmp")
      }

    case RemoveAttack(pointOnAttackView) =>
      log.info(s"RemoveAttack pointOnView:$pointOnAttackView")
      val attack = findTentacleNearTo(pointOnAttackView, tempWorld.attacks)
      attack match {
        case Some(tentacle) =>
          log.debug(s"Removing this attack: $tentacle ...")
          tempWorld = tempWorld -- tentacle
          parentActor ! DistributedStateMessages.UpdateWorld(tempWorld)
        case tmp@_ => log.debug(s"No attack detected $tmp")
      }
  }
}

/**
  * Companion object, containing actor messages
  */
object GameViewActor {
  def apply(parentActor: ActorRef): GameViewActor = new GameViewActor(parentActor)

  /**
    * Shows the GUI
    */
  case object ShowGUI

  /**
    * Hides the GUI
    */
  case object HideGUI

  /**
    * Sets a new world to display
    *
    * @param world the newWorld from which to compute new evolution
    */
  case class NewWorld(world: CellWorld)

  /**
    * Updates local version of the world making it "move"
    */
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

  /**
    * A method to find the tentacle near to a clicked point on view, according to actual tentacle sizing
    *
    * @param clickedPoint the cliked point on view
    * @param tentacles    the collection of attacks on screen
    * @return optionally the tentacle near the clicked point
    */
  private def findTentacleNearTo(clickedPoint: Point, tentacles: Seq[Tentacle]): Option[Tentacle] =
    tentacles.find(tentacle => GeometricUtils.
      pointDistanceFromStraightLine(clickedPoint, tentacle.from.position, tentacle.to.position) <= TentacleView.thicknessStrategy(tentacle))
}
