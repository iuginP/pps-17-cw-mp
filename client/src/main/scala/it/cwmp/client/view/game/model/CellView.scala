package it.cwmp.client.view.game.model

import java.awt.Color

import com.github.tkqubo.colorHash.ColorHash
import it.cwmp.client.model.game.SizingStrategy
import it.cwmp.client.model.game.impl.{Cell, Point}
import it.cwmp.client.view.game.ColoringStrategy

import scala.language.implicitConversions

/**
  * Classe che rappresenta una cella
  *
  * @author Davide Borficchia
  * @author Eugenio Pierfederici
  * @param center punto nel quale verrà disegnata la cella
  * @param radius dimensione della cella
  * @param energy energia della cella
  */
case class CellView(center: Point, radius: Double, color: Color, energy: Double)

/**
  * Companion object
  */
object CellView {

  /**
    * @return the ViewCell corresponding to the given Cell
    */
  implicit def cellToViewCell(cell: Cell): CellView = CellView(cell.position, sizingStrategy(cell), coloringStrategy(cell), cell.energy)

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
    * The default sizing strategy; returns the radius that the cellView should have
    *
    * Maps energy to radius
    */
  val sizingStrategy: SizingStrategy[Cell, Double] = _.energy
}
