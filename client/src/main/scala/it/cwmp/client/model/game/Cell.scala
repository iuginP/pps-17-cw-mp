package it.cwmp.client.model.game

import it.cwmp.client.view.game.model.ViewCell
import it.cwmp.model.User

/**
  * A trait describing a game Cell
  *
  * @author Enrico Siboni
  */
trait Cell {
  def owner: User

  def owner_=(newUser: User)

  def position: Point

  def energy: Int

  def energy_=(newEnergy: Int)
}

/**
  * Companion object
  */
object Cell {

  def apply(owner: User, energy: Int, position: Point): Cell = CellDefault(owner, energy, position)

  /**
    * Default implementation of Cell
    *
    * @param owner    the owner of the cell
    * @param energy   the energy of the cell
    * @param position the position of the cell
    */
  private case class CellDefault(var owner: User, var energy: Int, position: Point) extends Cell

  /**
    * @return the ViewCell corresponding to the given Cell
    */
  def toViewCell(cell: Cell, sizingStrategy: CellSizingStrategy, colouringStrategy: CellColouringStrategy): ViewCell = {
    ViewCell(cell.position, sizingStrategy.diameterOf(cell), colouringStrategy.colorOf(cell))
  }

}