package it.cwmp.client.model.game

import it.cwmp.client.view.game.Constants

/**
  * A trait describing the Cell sizing strategy
  */
trait CellSizingStrategy {
  def diameterOf(cell: Cell): Int
}

/**
  * Companion Object
  */
object CellSizingStrategy {

  /**
    * The default sizing strategy
    *
    * doubles size when cell has more than 100 energy
    */
  val defaultSizingStrategy: CellSizingStrategy = CellSizingStrategyDefault()

  private case class CellSizingStrategyDefault() extends CellSizingStrategy {
    override def diameterOf(cell: Cell): Int =
      if (cell.energy > 100) Constants.cellSize * 2 else Constants.cellSize
  }

}