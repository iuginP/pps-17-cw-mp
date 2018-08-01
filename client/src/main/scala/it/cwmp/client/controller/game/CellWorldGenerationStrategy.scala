package it.cwmp.client.controller.game

import java.time.Instant

import it.cwmp.client.controller.game.CellWorldGenerationStrategy.{MINIMUM_DISTANCE_BETWEEN_CELLS, MINIMUM_DISTANCE_FROM_BORDER, randomPoint}
import it.cwmp.client.model.game.GeometricUtils
import it.cwmp.client.model.game.impl.{Cell, CellWorld, Point}
import it.cwmp.client.view.game.model.CellView
import it.cwmp.model.User

import scala.util.Random

/**
  * A class implementing a CellWorld generation strategy
  *
  * @param worldHeight  the height of the world
  * @param worldWidth   the width of the world
  * @param passiveCells the number of passive cells to generate
  * @author Enrico Siboni
  */
case class CellWorldGenerationStrategy(worldHeight: Int, worldWidth: Int, passiveCells: Int) extends GenerationStrategy[Seq[User], CellWorld] {
  require(worldHeight > 0, "World height should be greater that 0")
  require(worldWidth > 0, "World width should be greater that 0")
  require(passiveCells >= 0, "Passive cells number should be positive")

  /**
    * Generates the world starting from a participant list
    *
    * @param players the participants to game
    * @return the generated world starting from participants
    */
  override def apply(players: Seq[User]): CellWorld =
    CellWorld(Instant.now(), generateWorldCells(players, passiveCells), Seq())

  /**
    * A method to generate world cells in a way that they don't overlap
    *
    * @param players              the participants to game for which to generate cells
    * @param numberOfPassiveCells the number of passive cells to generate among others
    * @return generated cells
    */
  def generateWorldCells(players: Seq[User], numberOfPassiveCells: Int): Seq[Cell] = {
    val participantCells = getParticipantCells(players)
    participantCells ++ randomPassiveCells(numberOfPassiveCells, participantCells.map(_.position))
  }

  /**
    * A method to position participant cells into game world
    *
    * @param players the participants to assign a cell into the world
    * @return the cells assigned to participants
    */
  def getParticipantCells(players: Seq[User]): Seq[Cell] = {
    var assignedPoints: Seq[Point] = Seq()
    for (player <- players) yield {
      val validPoint = getValidPoint(assignedPoints)
      assignedPoints = assignedPoints :+ validPoint
      Cell(player, validPoint)
    }
  }

  /**
    * A method to generate a certain number of passive cells to be placed into world
    *
    * @param numberOfPassiveCells  the number of passive cells to place
    * @param alreadyOccupiedPlaces those places already occupied
    * @return the passive cells placed
    */
  def randomPassiveCells(numberOfPassiveCells: Int, alreadyOccupiedPlaces: Seq[Point]): Seq[Cell] = {
    var assignedPoints: Seq[Point] = alreadyOccupiedPlaces
    for (_ <- 0 until numberOfPassiveCells) yield {
      val validPoint = getValidPoint(assignedPoints)
      assignedPoints = assignedPoints :+ validPoint
      Cell(Cell.Passive.NO_OWNER, validPoint, GameConstants.PASSIVE_CELL_ENERGY_WHEN_BORN)
    }
  }

  /**
    * A method to get a valid point for a cell;
    * that is a point which is not overlapping other cells and distant from world border
    *
    * @param alreadySelectedPoints the already selected points
    * @return the point that is valid according to rules
    */
  private def getValidPoint(alreadySelectedPoints: Seq[Point]): Point = {
    var point = randomPoint(worldWidth, worldHeight)
    while (!pointValid(point, alreadySelectedPoints)) point = randomPoint(worldWidth, worldHeight)
    point
  }

  /**
    * Determines if the point in which to place a cell is valid;
    *
    * A point is valid when it's distance from borders is respected and also the distance from other cells is respected
    *
    * @param toCheck         the point to check
    * @param alreadySelected the points representing already selected points
    * @return
    */
  private def pointValid(toCheck: Point, alreadySelected: Seq[Point]): Boolean =
    toCheck.x >= MINIMUM_DISTANCE_FROM_BORDER && toCheck.x <= worldWidth - MINIMUM_DISTANCE_FROM_BORDER &&
      toCheck.y >= MINIMUM_DISTANCE_FROM_BORDER && toCheck.y <= worldHeight - MINIMUM_DISTANCE_FROM_BORDER &&
      alreadySelected.forall(point => !GeometricUtils.isWithinCircumference(toCheck, point, MINIMUM_DISTANCE_BETWEEN_CELLS))
}

/**
  * Companion object
  */
object CellWorldGenerationStrategy {
  private val MINIMUM_DISTANCE_FROM_BORDER = CellView.CELL_MAX_RADIUS_FOR_ENERGY._1
  private val MINIMUM_DISTANCE_BETWEEN_CELLS = CellView.CELL_MAX_RADIUS_FOR_ENERGY._1 * 2

  /**
    * Returns a random point with coordinates between 0 and max values (inclusive)
    *
    * @param maxX the max X of any generated point
    * @param maxY the max Y of any generated point
    * @return the generated point
    */
  private def randomPoint(maxX: Int, maxY: Int): Point = Point(Random.nextInt(maxX + 1), Random.nextInt(maxY + 1))
}
