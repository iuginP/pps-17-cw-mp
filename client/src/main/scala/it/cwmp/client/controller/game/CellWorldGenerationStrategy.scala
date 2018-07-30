package it.cwmp.client.controller.game

import java.time.Instant

import it.cwmp.client.controller.game.CellWorldGenerationStrategy.{MINIMUM_DISTANCE_BETWEEN_CELLS, MINIMUM_DISTANCE_FROM_BORDER}
import it.cwmp.client.model.game.impl.{Cell, CellWorld, Point}
import it.cwmp.client.view.game.model.CellView
import it.cwmp.model.Participant

import scala.util.Random

/**
  * A class implementing a CellWorld generation strategy
  *
  * @author Enrico Siboni
  */
case class CellWorldGenerationStrategy(worldHeight: Int, worldWidth: Int) extends GenerationStrategy[Seq[Participant], CellWorld] {

  /**
    * Generates the world starting from a participant list
    *
    * @param participants the participants to game
    * @return the generated world starting from participants
    */
  override def apply(participants: Seq[Participant]): CellWorld =
    CellWorld(Instant.now(), getParticipantCells(participants), Seq())

  /**
    * A method to position participant cells into game world
    *
    * @param participants the participants to assign a cell into the world
    * @return the cells assigned to participants
    */
  def getParticipantCells(participants: Seq[Participant]): Seq[Cell] = {

    def randomPoint = Point(Random.nextInt(worldWidth + 1), Random.nextInt(worldHeight + 1))

    var selectedPoints: Seq[Point] = Seq()
    for (participant <- participants) yield {
      var toCheck = randomPoint
      while (!pointValid(toCheck, selectedPoints)) toCheck = randomPoint
      selectedPoints = selectedPoints :+ toCheck
      Cell(participant, randomPoint)
    }
  }

  /**
    * Determines if the point to check is valid;
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
      alreadySelected.forall(point =>
        (toCheck.x >= point.x + MINIMUM_DISTANCE_BETWEEN_CELLS || toCheck.x <= point.x - MINIMUM_DISTANCE_BETWEEN_CELLS)
          && (toCheck.y >= point.y + MINIMUM_DISTANCE_BETWEEN_CELLS || toCheck.y <= point.y - MINIMUM_DISTANCE_BETWEEN_CELLS))
}

/**
  * Companion object
  */
object CellWorldGenerationStrategy {
  private val MINIMUM_DISTANCE_FROM_BORDER = CellView.CELL_MAX_RADIUS_FOR_ENERGY._1
  private val MINIMUM_DISTANCE_BETWEEN_CELLS = CellView.CELL_MAX_RADIUS_FOR_ENERGY._1 * 2
}
