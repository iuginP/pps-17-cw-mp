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
    * The energy that a cell has when is born
    */
  val whenBornEnergy = 20

  /**
    * Coverts this amount of time to an energy unit
    */
  val TIME_TO_ENERGY_CONVERSION_RATE = 1000d

  /**
    * The default evolution strategy for cells
    *
    * adds 1 to energy each second
    */
  val defaultEvolutionStrategy: EvolutionStrategy[Cell, Duration] = (cell: Cell, elapsedTime: Duration) => {
    cell ++ (elapsedTime.toMillis / TIME_TO_ENERGY_CONVERSION_RATE)
  }

  /**
    * Returns the distance between two cells
    *
    * @param cell1 the first cell
    * @param cell2 the second cell
    * @return the distance
    */
  def distance(cell1: Cell, cell2: Cell): Long = {
    GeometricUtils.distance(cell1.position, cell2.position).toLong
  }

  /**
    * @return true if provided cells have matching owner and position
    */
  def ownerAndPositionMatch(cell1: Cell, cell2: Cell): Boolean =
    cell1.owner == cell2.owner && cell1.position == cell2.position

  /**
    * A class to manipulate cells
    *
    * @param cell the cell to manipulate
    */
  implicit class CellManipulator(cell: Cell) {

    /**
      * Evolves the cell according to elapsedTime and evolutionStrategy
      *
      * @param elapsedTime           elapsedTime to consider
      * @param cellEvolutionStrategy the evolution strategy to consider
      * @return
      */
    def evolve(elapsedTime: Duration, cellEvolutionStrategy: EvolutionStrategy[Cell, Duration] = defaultEvolutionStrategy): Cell =
      cellEvolutionStrategy(cell, elapsedTime)

    /**
      * Creates a new cell with energy reduced
      *
      * @param energyReduction the amount of energy to remove
      * @return the cell with reduced energy
      */
    def --(energyReduction: Double): Cell = {
      require(energyReduction >= 0, "Energy decrement should be positive")
      if (energyReduction == 0) cell
      else Cell(cell.owner, cell.position, cell.energy - energyReduction)
    }

    /**
      * Creates a new cell with energy increased
      *
      * @param energyIncrement the increment to add to energy
      * @return the cell with incremented energy
      */
    def ++(energyIncrement: Double): Cell = {
      require(energyIncrement >= 0, "Energy increment should be positive")
      if (energyIncrement == 0) cell
      else Cell(cell.owner, cell.position, cell.energy + energyIncrement)
    }

  }

}