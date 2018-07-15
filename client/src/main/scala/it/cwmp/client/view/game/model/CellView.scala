package it.cwmp.client.view.game.model

import java.awt.Color

import com.github.tkqubo.colorHash.ColorHash
import it.cwmp.client.model.game.impl.{Cell, Point}
import it.cwmp.client.model.game.{ColoringStrategy, SizingStrategy}
import it.cwmp.client.view.game.Constants._

import scala.language.implicitConversions

/**
  * Classe che rappresenta una cella
  *
  * @author Davide Borficchia
  * @author Eugenio Pierfederici
  * @param center punto nel quale verrà disegnata la cella
  * @param size   dimensione della cella
  */
case class CellView(center: Point, color: Color = defaultColor, size: Int = cellSize)

/**
  * Companion object
  */
object CellView {

  /**
    * @return the ViewCell corresponding to the given Cell
    */
  implicit def cellToViewCell(cell: Cell): CellView = CellView(cell.position, coloringStrategy(cell), sizingStrategy(cell))

  /**
    * Default cell coloring strategy
    *
    * Color based on the username hash value
    */
  val coloringStrategy: ColoringStrategy[Cell, Color] = (cell: Cell) => {
    val color = new ColorHash().rgb(cell.owner.username)
    new Color(color.red, color.green, color.blue)
  }

  /**
    * The default sizing strategy
    *
    * Maps size to energy
    */
  val sizingStrategy: SizingStrategy[Cell, Int] = _.energy
}
