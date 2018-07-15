package it.cwmp.client.model.game.impl

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
case class Cell(var owner: User,
                position: Point,
                var energy: Int) extends Character[User, Point, Int] {

  requireNonNull(owner, "User owner must not be null")
  requireNonNull(position, "Position must not be null")
  require(energy > 0, "Energy must be positive")
}


/**
  * Companion object
  */
object Cell {

  /**
    * The default evolution strategy for cells
    *
    * adds 1 to energy each second
    */
  val cellEvolutionStrategy: EvolutionStrategy[Cell, Long] = new EvolutionStrategy[Cell, Long] {
    private var residualTime: Long = 0

    override def apply(elapsedTime: Long, cell: Cell): Cell = {
      cell.energy = cell.energy + ((residualTime + elapsedTime) / 1000).toInt
      residualTime = (residualTime + elapsedTime) % 1000
      cell
    }
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
}