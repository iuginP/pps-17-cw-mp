package it.cwmp.client.view.game

import akka.actor.{Actor, ActorRef, Cancellable}
import it.cwmp.client.controller.ViewVisibilityMessages.Hide
import it.cwmp.client.controller.game.GameEngine
import it.cwmp.client.controller.messages.Initialize
import it.cwmp.client.model.game.impl.LWWCellWorld.UpdateState
import it.cwmp.client.model.game.GeometricUtils
import it.cwmp.client.model.game.impl._
import it.cwmp.client.view.game.GameViewActor._
import it.cwmp.client.view.game.model.{CellView, TentacleView}
import it.cwmp.utils.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * The actor that deals with Game View
  *
  * @author contributor Enrico Siboni
  */
case class GameViewActor() extends Actor with Logging {

  private val gameFX: GameFX = GameFX(self)
  private val TIME_BETWEEN_FRAMES: FiniteDuration = 350.millis

  private var parentActor: ActorRef = _
  private var updatingSchedule: Cancellable = _
  private var tempWorld: CellWorld = _
  private var playerName: String = _

  override def receive: Receive = showGUIBehaviour orElse {
    case Initialize => parentActor = sender()
  }

  /**
    * The behaviour of opening the view
    */
  private def showGUIBehaviour: Receive = {
    case ShowGUIWithName(name) =>
      playerName = name
      gameFX.start(s"$VIEW_TITLE_PREFIX$name", VIEW_SIZE)
      context.become(hideGUIBehaviour orElse newWorldBehaviour orElse guiWorldModificationsBehaviour)
  }

  /**
    * The behaviour of closing the view
    */
  private def hideGUIBehaviour: Receive = { // TODO: remove, no-one ever sends this message here
    case Hide =>
      if (updatingSchedule != null) updatingSchedule.cancel()
      gameFX.close()
      context.become(showGUIBehaviour)
  }

  /**
    * The behaviour of receiving world modifications from external source
    */
  private def newWorldBehaviour: Receive = {
    case NewWorld(world) =>
      tempWorld = world
      if (updatingSchedule == null) {
        updatingSchedule = context.system.scheduler
          .schedule(TIME_BETWEEN_FRAMES, TIME_BETWEEN_FRAMES, self, UpdateGUI)(context.dispatcher)
      }

    case UpdateGUI =>
      Future {
        tempWorld = GameEngine(tempWorld, java.time.Duration.ofMillis(TIME_BETWEEN_FRAMES.toMillis))
        tempWorld
      } andThen {
        case Success(cellWorld) => gameFX.updateWorld(cellWorld)
        case Failure(ex) =>
          updatingSchedule.cancel()
          log.error("Error calculating next CellWorld", ex)
      }
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
        case (Some(attacker), Some(attacked)) if canAddAttack(attacker, attacked, tempWorld.attacks) =>
          log.debug(s"Adding attack from $attacker to $attacked ...")
          parentActor ! UpdateState(tempWorld ++ Tentacle(attacker, attacked, tempWorld.instant))
        case tmp@_ => log.debug(s"No cells detected or auto-attack or not $playerName cell $tmp")
      }

    case RemoveAttack(pointOnAttackView) =>
      log.info(s"RemoveAttack pointOnView:$pointOnAttackView")
      val attack = findTentacleNearTo(pointOnAttackView, tempWorld.attacks)
      attack match {
        case Some(tentacle) if tentacle.from.owner.username == playerName =>
          log.debug(s"Removing this attack: $tentacle ...")
          parentActor ! UpdateState(tempWorld -- tentacle)
        case tmp@_ => log.debug(s"No attack detected or not $playerName attack $tmp")
      }
  }

  /**
    * Utility method to know if can add attack
    *
    * @param attacker       the attacker cell
    * @param attacked       the attacked cell
    * @param currentAttacks the current game attacks
    * @return true if attack can be added, false otherwise
    */
  private def canAddAttack(attacker: Cell, attacked: Cell, currentAttacks: Seq[Tentacle]): Boolean = {
    !Cell.ownerAndPositionMatch(attacker, attacked) && // no auto-attack
      attacker.owner.username == playerName && // control only your cells
      !currentAttacks.exists(tentacle => // no already present attacks
        Cell.ownerAndPositionMatch(tentacle.from, attacker) &&
          Cell.ownerAndPositionMatch(tentacle.to, attacked))
  }
}

/**
  * Companion object, containing actor messages
  */
object GameViewActor {

  /**
    * The title of game view
    */
  val VIEW_TITLE_PREFIX = "CellWars: "

  /**
    * The size of the squared view
    */
  val VIEW_SIZE = 512

  /**
    * Shows the GUI
    */
  case class ShowGUIWithName(playerName: String)

  /**
    * Sets a new world to display
    *
    * @param world the newWorld from which to compute new evolution
    */
  case class NewWorld(world: CellWorld)

  /**
    * Updates GUI of the world making it "move"
    */
  case object UpdateGUI

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
    * @param clickedPoint the clicked point on view
    * @param tentacles    the collection of attacks on screen
    * @return optionally the tentacle near the clicked point
    */
  private def findTentacleNearTo(clickedPoint: Point, tentacles: Seq[Tentacle]): Option[Tentacle] =
    tentacles.find(tentacle => GeometricUtils.
      pointDistanceFromStraightLine(clickedPoint, tentacle.from.position, tentacle.to.position) <= TentacleView.thicknessStrategy(tentacle))
}
