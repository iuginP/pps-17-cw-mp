package it.cwmp.client.view.game.model

import com.github.tkqubo.colorHash.ColorHash
import it.cwmp.client.controller.game.GameConstants
import it.cwmp.client.model.game.SizingStrategy
import it.cwmp.client.model.game.impl.{Cell, Point}
import it.cwmp.client.view.game.GameViewConstants.RGB_RANGE
import it.cwmp.client.view.game.{ColoringStrategy, GameViewConstants}
import javafx.scene.paint.Color
import javafx.scene.text.Font

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
    * The default font for energy text on cellViews
    */
  val ENERGY_DEFAULT_FONT: Font = Font.font("Verdana", GameViewConstants.GAME_DEFAULT_FONT_SIZE)

  private val CELL_VIEW_COLOR_OPACITY = 1
  private val CELL_DYING_FONT_COLOR = Color.DARKRED

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
    new Color(color.red / RGB_RANGE, color.green / RGB_RANGE, color.blue / RGB_RANGE, CELL_VIEW_COLOR_OPACITY)
  }

  /**
    * Default energy text coloring strategy
    */
  val energyTextColoringStrategy: ColoringStrategy[Cell, Color] = {
    case cell: Cell if cell.energy < GameConstants.CELL_ENERGY_WHEN_BORN => CELL_DYING_FONT_COLOR
    case _ => GameViewConstants.GAME_DEFAULT_FONT_COLOR
  }

  /**
    * The default sizing strategy; returns the radius that the cellView should have
    *
    * Maps energy to radius
    */
  val sizingStrategy: SizingStrategy[Cell, Double] = _.energy
}
