package it.cwmp.client.model.game.impl

import java.time.Duration
import java.util.Objects._

import it.cwmp.client.model.game._
import it.cwmp.model.User

/**
  * Default implementation of game Cell
  *
  * @param owner    the owner of the cell
  * @param energy   the energy of the cell
  * @param position the position of the cell
  * @author Enrico Siboni
  */
case class Cell(owner: User,
                position: Point,
                energy: Double) extends Character[User, Point, Double] {

  requireNonNull(owner, "User owner must not be null")
  requireNonNull(position, "Position must not be null")
  require(energy > 0, "Energy must be positive")
}


/**
  * Companion object
  */
object Cell {

  /**
    * Coverts this amount of time to an energy unit
    */
  val TIME_TO_ENERGY_CONVERSION_RATE = 1000d

  /**
    * The default evolution strategy for cells
    *
    * adds 1 to energy each second
    */
  val evolutionStrategy: EvolutionStrategy[Cell, Duration] = (cell: Cell, elapsedTime: Duration) => {
    Cell(cell.owner, cell.position, cell.energy + (elapsedTime.toMillis / TIME_TO_ENERGY_CONVERSION_RATE))
  }

  /**
    * Returns the distance between two cells
    *
    * @param cell1 the first cell
    * @param cell2 the second cell
    * @return the distance
    */
  def distance(cell1: Cell, cell2: Cell): Long = {
    Point.distance(cell1.position, cell2.position)
  }

  /**
    * @return true if provided cells have matching owner and position
    */
  def matchOwnerAndPosition(cell1: Cell, cell2: Cell): Boolean =
    cell1.owner == cell2.owner && cell1.position == cell2.position
}