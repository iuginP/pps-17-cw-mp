package it.cwmp.client.model.game

import it.cwmp.client.view.game.Constants
import it.cwmp.client.view.game.model.ViewCell
import it.cwmp.model.User
import javafx.scene.paint.Color

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

  def apply(owner: User, position: Point, energy: Int): Cell = CellDefault(owner, position, energy)

  /**
    * Default implementation of Cell
    *
    * @param owner    the owner of the cell
    * @param energy   the energy of the cell
    * @param position the position of the cell
    */
  case class CellDefault(var owner: User, position: Point, var energy: Int) extends Cell

  /**
    * @return the ViewCell corresponding to the given Cell
    */
  def toViewCell(cell: Cell,
                 sizingStrategy: SizingStrategy[Cell, Int] = defaultSizingStrategy,
                 coloringStrategy: ColoringStrategy[Cell, Color] = defaultColoringStrategy): ViewCell = {
    ViewCell(cell.position, sizingStrategy.sizeOf(cell), coloringStrategy.colorOf(cell))
  }

  /**
    * Default cell coloring strategy
    */
  val defaultColoringStrategy = CellColouringStrategy()

  private case class CellColouringStrategy() extends ColoringStrategy[Cell, Color] {
    override def colorOf(cell: Cell): Color =
      cell.owner.username.charAt(0) match {
        case 'd' => Color.GREEN
        case 'e' => Color.RED
        case _ => Color.BROWN
      }
  }

  /**
    * The default sizing strategy
    *
    * doubles size when cell has more than 100 energy
    */
  val defaultSizingStrategy: SizingStrategy[Cell, Int] = CellSizingStrategy()

  private case class CellSizingStrategy() extends SizingStrategy[Cell, Int] {
    /**
      * @return the diameter of the cell
      */
    override def sizeOf(cell: Cell): Int =
      if (cell.energy > 100) Constants.cellSize * 2 else Constants.cellSize
  }

  /**
    * The default evolution strategy for cells
    *
    * adds 1 to energy each second
    */
  val cellEvolutionStrategy = EvolutionStrategyDefault()

  private case class EvolutionStrategyDefault() extends EvolutionStrategy[Cell, Long] {
    private var residualTime: Long = 0

    override def evolveAccordingTo(elapsedTime: Long, cell: Cell): Cell = {
      cell.energy = cell.energy + ((residualTime + elapsedTime) / 1000).toInt
      residualTime = (residualTime + elapsedTime) % 1000
      cell
    }
  }

}