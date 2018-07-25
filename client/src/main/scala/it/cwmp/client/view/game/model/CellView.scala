package it.cwmp.client.view.game.model

import com.github.tkqubo.colorHash.ColorHash
import it.cwmp.client.controller.game.GameConstants
import it.cwmp.client.model.game.impl.{Cell, Point}
import it.cwmp.client.model.game.{GeometricUtils, SizingStrategy}
import it.cwmp.client.view.game.GameViewConstants.RGB_RANGE
import it.cwmp.client.view.game.{ColoringStrategy, GameViewConstants}
import javafx.scene.paint.Color
import javafx.scene.text.Font

import scala.language.implicitConversions

/**
  * A class representing the View counterpart of a Cell
  *
  * @param center          the center point where the CellView will be placed
  * @param radius          the radius of the cell
  * @param color           the color of the cellView
  * @param energy          the cell energy
  * @param energyTextColor the color of the energyText
  * @author Davide Borficchia
  * @author Eugenio Pierfederici
  */
case class CellView(center: Point, radius: Double, color: Color, energy: Double, energyTextColor: Color)

/**
  * Companion object
  *
  * @author Enrico Siboni
  */
object CellView {

  /**
    * The default font for energy text on cellViews
    */
  val ENERGY_DEFAULT_FONT: Font = Font.font("Verdana", GameViewConstants.GAME_DEFAULT_FONT_SIZE)

  private val CELL_VIEW_COLOR_OPACITY = 1
  private val CELL_DYING_FONT_COLOR = Color.DARKRED

  private val CELL_MINIMUM_RADIUS_FOR_ENERGY = (25d, 20d)
  private val CELL_MAX_RADIUS_FOR_ENERGY = (45d, 100d)

  /**
    * @return the ViewCell corresponding to the given Cell
    */
  implicit def cellToView(cell: Cell): CellView =
    CellView(cell.position, sizingStrategy(cell), coloringStrategy(cell), cell.energy, energyTextColoringStrategy(cell))

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
    */
  val sizingStrategy: SizingStrategy[Cell, Double] = {
    case cell: Cell if cell.energy <= CELL_MINIMUM_RADIUS_FOR_ENERGY._2 => CELL_MINIMUM_RADIUS_FOR_ENERGY._1
    case cell: Cell if cell.energy >= CELL_MAX_RADIUS_FOR_ENERGY._2 => CELL_MAX_RADIUS_FOR_ENERGY._1
    case cell: Cell => radiusBetweenMinimumAndMaximum(cell, CELL_MINIMUM_RADIUS_FOR_ENERGY, CELL_MAX_RADIUS_FOR_ENERGY)
  }

  /**
    * A method to calculate the radius of the cell between maximum and minimum provided
    *
    * @param cell                   the cell to draw
    * @param minimumRadiusAndEnergy the lower bound values
    * @param maximumRadiusAndEnergy the upper bound values
    * @return the sized cell radius
    */
  private def radiusBetweenMinimumAndMaximum(cell: Cell,
                                             minimumRadiusAndEnergy: (Double, Double),
                                             maximumRadiusAndEnergy: (Double, Double)): Double = {
    val energyDeltaFromMinimum = cell.energy - minimumRadiusAndEnergy._2
    val minimumPoint = Point(minimumRadiusAndEnergy._1.toInt, minimumRadiusAndEnergy._2.toInt)
    val maximumPoint = Point(maximumRadiusAndEnergy._1.toInt, maximumRadiusAndEnergy._2.toInt)

    // I use geometric utils to calculate a pair (a point) between minimum and maximum pairs, related to energy delta
    // I'm interested in first component of the delta because it represents the radius
    val radiusDelta = GeometricUtils.deltaXYFromFirstPoint(minimumPoint, maximumPoint, energyDeltaFromMinimum)._1
    minimumRadiusAndEnergy._1 + radiusDelta
  }
}
