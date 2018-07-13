package it.cwmp.client.model.game

import javafx.scene.paint.Color

/**
  * A trait that describes how to color Cells
  */
trait CellColouringStrategy {
  def colorOf(cell: Cell): Color
}

/**
  * Companion object
  */
object CellColouringStrategy {

  /**
    * The default colouring strategy
    *
    * Explain in which way colours cells
    */
  val defaultColoringStrategy: CellColouringStrategy = CellColouringStrategyDefault()

  private case class CellColouringStrategyDefault() extends CellColouringStrategy {
    override def colorOf(cell: Cell): Color =
      cell.owner.username.charAt(0) match {
        case 'd' => Color.GREEN
        case 'e' => Color.RED
        case _ => Color.BROWN
      }
  }

}