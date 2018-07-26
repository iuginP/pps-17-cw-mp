package it.cwmp.client.view.game

import akka.actor.{Actor, ActorRef, Cancellable}
import it.cwmp.client.controller.game.GameEngine
import it.cwmp.client.model.DistributedState
import it.cwmp.client.model.game.GeometricUtils
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

  private val gameFX: GameFX = GameFX(self)
  private val FRAME_RATE: FiniteDuration = 50.millis

  private var updatingSchedule: Cancellable = _
  private var tempWorld: CellWorld = _

  override def receive: Receive = showGUIBehaviour

  /**
    * The behaviour of opening the view
    */
  private def showGUIBehaviour: Receive = {
    case ShowGUI =>
      gameFX.start(VIEW_TITLE, VIEW_SIZE)
      context.become(hideGUIBehaviour orElse
        newWorldBehaviour orElse guiWorldModificationsBehaviour)
  }

  /**
    * The behaviour of closing the view
    */
  private def hideGUIBehaviour: Receive = {
    case HideGUI =>
      gameFX.close()
      context.become(showGUIBehaviour)
  }

  /**
    * The behaviour of receiving world modifications from external source
    */
  private def newWorldBehaviour: Receive = {
    case NewWorld(world) =>
      if (updatingSchedule != null) updatingSchedule.cancel()
      tempWorld = world
      updatingSchedule = context.system.scheduler
        .schedule(0.millis, FRAME_RATE, self, UpdateLocalWorld)(context.dispatcher)

    case UpdateLocalWorld =>
      //  log.info(s"World to paint: Characters=${tempWorld.characters} Attacks=${tempWorld.attacks} Instant=${tempWorld.instant}")
      gameFX.updateWorld(tempWorld)

      // is that to heavy computation here ???
      tempWorld = GameEngine(tempWorld, java.time.Duration.ofMillis(FRAME_RATE.toMillis))
  }

  /**
    * The behaviour of listening for user events on GUI
    */
  private def guiWorldModificationsBehaviour: Receive = {
    case AddAttack(from, to) =>
      log.info(s"AddAttack from:$from to:$to")
      val worldCharacters = tempWorld.characters
      val fromCell = findCellNearTo(from, worldCharacters)
      val toCell = findCellNearTo(to, worldCharacters)
      (fromCell, toCell) match {
        case (Some(attacker), Some(attacked)) if attacker != attacked =>
          log.debug(s"Adding attack from $attacker to $attacked ...")
          parentActor ! DistributedState.UpdateState(tempWorld ++ Tentacle(attacker, attacked, tempWorld.instant))
        case tmp@_ => log.debug(s"No cells detected or auto-attack $tmp")
      }

    case RemoveAttack(pointOnAttackView) =>
      log.info(s"RemoveAttack pointOnView:$pointOnAttackView")
      val attack = findTentacleNearTo(pointOnAttackView, tempWorld.attacks)
      attack match {
        case Some(tentacle) =>
          log.debug(s"Removing this attack: $tentacle ...")
          parentActor ! DistributedState.UpdateState(tempWorld -- tentacle)
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
    * The title of game view
    */
  val VIEW_TITLE = "CellWars"

  /**
    * The size of the squared view
    */
  val VIEW_SIZE = 512 // TODO: sarebbe buono forse fare una dimensione diversa in base alla dimensione dello schermo

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
