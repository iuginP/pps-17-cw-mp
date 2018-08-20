package it.cwmp.client.controller.actors.view

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorRef, Cancellable}
import it.cwmp.client.controller.actors.common.{ActorAlertManagement, AlertMessages}
import it.cwmp.client.controller.actors.messages.Initialize
import it.cwmp.client.controller.actors.view.GameViewActor._
import it.cwmp.client.controller.game.GameConstants.{MAX_TIME_BETWEEN_CLIENT_SYNCHRONIZATION, MIN_TIME_BETWEEN_CLIENT_SYNCHRONIZATION}
import it.cwmp.client.controller.game.GameEngine
import it.cwmp.client.model.game.distributed.AkkaDistributedState.UpdateState
import it.cwmp.client.model.game.impl._
import it.cwmp.client.utils.GeometricUtils
import it.cwmp.client.view.game.{CellView, GameFXController, TentacleView}
import it.cwmp.client.view.{FXAlertsController, FXRunOnUIThread}
import it.cwmp.utils.Logging
import it.cwmp.utils.Utils.stringToOption

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * The actor that deals with Game View
  *
  * @author contributor Enrico Siboni
  */
case class GameViewActor() extends Actor with FXRunOnUIThread with ActorAlertManagement with Logging {

  private val TIME_BETWEEN_FRAMES: FiniteDuration = 350.millis
  private val WAIT_TIME_BEFORE_AUTOMATIC_SYNCHRONIZATION = ThreadLocalRandom.current()
    .nextInt(MIN_TIME_BETWEEN_CLIENT_SYNCHRONIZATION, MAX_TIME_BETWEEN_CLIENT_SYNCHRONIZATION).millis

  private var gameFX: GameFXController = _
  private var parentActor: ActorRef = _
  private var updatingSchedule: Cancellable = _
  private var synchronizationSchedule: Cancellable = _
  private var tempWorld: CellWorld = _
  private var playerName: String = _

  override protected def fxController: FXAlertsController = gameFX

  override def receive: Receive = showGUIBehaviour orElse {
    case Initialize => parentActor = sender()
  }

  override protected def onInfoAlertReceived(title: String, message: String): Unit = {
    super.onInfoAlertReceived(title, message)
    context.become(newWorldBehaviour orElse guiWorldModificationsBehaviour)
  }

  /**
    * The behaviour of opening the view
    */
  private def showGUIBehaviour: Receive = {
    case ShowGUIWithName(name) =>
      playerName = name

      runOnUIThread(() => {
        gameFX = GameFXController(self, VIEW_TITLE_PREFIX + name, VIEW_SIZE, name)
        gameFX.showGUI()
      })

      context.become(alertBehaviour orElse newWorldBehaviour orElse guiWorldModificationsBehaviour)
  }

  /**
    * The behaviour of receiving world modifications from external source
    */
  private def newWorldBehaviour: Receive = {
    case NewWorld(world) =>
      tempWorld = world
      restartSynchronizationSchedule(WAIT_TIME_BEFORE_AUTOMATIC_SYNCHRONIZATION)
      if (updatingSchedule == null) {
        updatingSchedule = context.system.scheduler
          .schedule(TIME_BETWEEN_FRAMES, TIME_BETWEEN_FRAMES, self, UpdateGUI)(context.dispatcher)
      }
      gameEnded(world) match {
        case Some(winnerName) if winnerName == playerName => self ! AlertMessages.Info(s"$playerName $YOU_WON_TITLE", YOU_WON)
        case Some(_) => self ! AlertMessages.Info(s"$playerName $YOU_LOST_TITLE", YOU_LOST)
        case None =>
      }

    case UpdateGUI =>
      Future {
        tempWorld = GameEngine(tempWorld, java.time.Duration.ofMillis(TIME_BETWEEN_FRAMES.toMillis))
        tempWorld
      } andThen {
        case Success(cellWorld) => runOnUIThread { () => gameFX.updateWorld(cellWorld) }
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
          restartSynchronizationSchedule(WAIT_TIME_BEFORE_AUTOMATIC_SYNCHRONIZATION)
        case tmp@_ => log.debug(s"No cells detected or auto-attack or not $playerName cell $tmp")
      }

    case RemoveAttack(pointOnAttackView) =>
      log.info(s"RemoveAttack pointOnView:$pointOnAttackView")
      val attacks = findTentaclesNearTo(pointOnAttackView, tempWorld.attacks)
      attacks.find(_.from.owner.username == playerName) match {
        case Some(tentacle) =>
          log.debug(s"Removing this attack: $tentacle ...")
          parentActor ! UpdateState(tempWorld -- tentacle)
          restartSynchronizationSchedule(WAIT_TIME_BEFORE_AUTOMATIC_SYNCHRONIZATION)
        case tmp@_ => log.debug(s"No attack detected or not $playerName attack $tmp")
      }

    case SynchronizeWorld =>
      log.info("Time to synchronize other clients because of no user actions")
      parentActor ! UpdateState(tempWorld)
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

  /**
    * Utility method to restart the world synchronization schedule
    */
  private def restartSynchronizationSchedule(waitTime: FiniteDuration): Unit = {
    if (synchronizationSchedule != null) synchronizationSchedule.cancel()

    synchronizationSchedule = context.system.scheduler
      .schedule(waitTime, waitTime, self, SynchronizeWorld)(context.dispatcher)
  }

  /**
    * Checks if the game has ended
    *
    * @param cellWorld the cellWorld to check
    * @return optionally the winner name
    */
  private def gameEnded(cellWorld: CellWorld): Option[String] =
    if (cellWorld.characters.forall(_.owner == cellWorld.characters.head.owner)) {
      cellWorld.characters.head.owner.username
    } else None
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
    * Makes Actor send a distributed world that's equal to current one
    */
  case object SynchronizeWorld

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

  private val YOU_WON_TITLE = "HAI VINTO!!!"
  private val YOU_WON = "Hai conquistato tutti gli avversari, complimenti"

  private val YOU_LOST_TITLE = "HAI PERSO..."
  private val YOU_LOST = "Sei stato sterminato dagli avversari, la prossima volta sarai più fortunato..."

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
    * @return the sequence of tentacles near the clicked point
    */
  private def findTentaclesNearTo(clickedPoint: Point, tentacles: Seq[Tentacle]): Seq[Tentacle] =
    tentacles.filter(tentacle => GeometricUtils.
      pointDistanceFromStraightLine(clickedPoint, tentacle.from.position, tentacle.to.position) <= TentacleView.thicknessStrategy(tentacle))
}
