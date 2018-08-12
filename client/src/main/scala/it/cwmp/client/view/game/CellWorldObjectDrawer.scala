package it.cwmp.client.view.game

import java.time.{Duration, Instant}

import it.cwmp.client.view.game.model._
import javafx.scene.layout._
import javafx.scene.paint.Color
import javafx.scene.shape.{Line, SVGPath}
import javafx.scene.text.Text

import scala.language.implicitConversions

/**
  * A trait that makes possible to draw View items
  *
  * @author Eugenio Pierfederici
  * @author Davide Borficchia
  */
trait CellWorldObjectDrawer {

  private var firstWorldInstantOption: Option[Instant] = None

  /**
    * A method to draw a CellView
    *
    * @param cell the cell to draw
    * @return the drawn region
    */
  def drawCell(cell: CellView): Region = {
    val svg = new SVGPath
    svg.setContent(CellView.CELL_VIEW_DEFAULT_SVG_SHAPE)
    val svgShape = new Region
    svgShape.setShape(svg)
    svgShape.setBorder(cell.border)
    svgShape.setPrefSize(cell.radius * 2, cell.radius * 2)
    svgShape.setStyle(getCellViewBackgroundStyle(cell.color))
    svgShape.setLayoutX(cell.center.x - cell.radius)
    svgShape.setLayoutY(cell.center.y - cell.radius)
    svgShape
  }

  /**
    * A method to draw the cell energy
    *
    * @param cell the cell of which energy is to be drawn
    * @return the text to add to scene
    */
  def drawCellEnergy(cell: CellView): Text = {
    val energyText = new Text(cell.energy.toInt.toString)
    energyText.setFont(CellView.ENERGY_DEFAULT_FONT)
    energyText.setFill(cell.energyTextColor)
    energyText.setX(cell.center.x - (energyText.getLayoutBounds.getWidth / 2))
    energyText.setY(cell.center.y + (energyText.getLayoutBounds.getHeight / 2))
    energyText
  }

  /**
    * A method to draw a tentacle on GUI
    *
    * @param tentacle the tentacle View to draw
    * @return the line to add in GUI
    */
  def drawTentacle(tentacle: TentacleView): Line = {
    val line = new Line()
    line.setStroke(tentacle.color)
    line.setStrokeWidth(tentacle.thickness)

    val attackerPosition = tentacle.startPoint
    val tentacleReachedPoint = tentacle.reachedPoint
    line.setStartX(attackerPosition.x)
    line.setStartY(attackerPosition.y)
    line.setEndX(tentacleReachedPoint.x)
    line.setEndY(tentacleReachedPoint.y)
    line
  }

  /**
    * A method to draw elapsed time on GUI
    *
    * @param actualWorldInstant the actual World Instant
    * @param viewWidth          the view Width
    * @return the text to draw
    */
  def drawInstant(actualWorldInstant: Instant, viewWidth: Double): Text = {
    val elapsedTimeFromBeginning: Duration = firstWorldInstantOption match {
      case Some(firstWorldInstant) => Duration.between(firstWorldInstant, actualWorldInstant)
      case None =>
        firstWorldInstantOption = Some(actualWorldInstant)
        Duration.ofSeconds(0)
    }
    val minutes = elapsedTimeFromBeginning.getSeconds / 60
    val seconds = elapsedTimeFromBeginning.getSeconds % 60
    val instantText = new Text(GameViewConstants.GAME_TIME_TEXT_FORMAT.format(minutes, seconds))
    instantText.setFont(GameViewConstants.GAME_TIME_TEXT_FONT)
    instantText.setFill(GameViewConstants.GAME_TIME_TEXT_COLOR)
    instantText.setX((viewWidth / 2) - (instantText.getLayoutBounds.getWidth / 2))
    instantText.setY(instantText.getLayoutBounds.getHeight)
    instantText
  }

  /**
    * Converter from JavaFX Colors to JavaAWT colors
    *
    * @param fxColor the JavaFX color to convert
    * @return the JAvaAWT correspondent
    */
  private implicit def fxColorToAwtColor(fxColor: javafx.scene.paint.Color): java.awt.Color = {
    new java.awt.Color(fxColor.getRed.toFloat, fxColor.getGreen.toFloat, fxColor.getBlue.toFloat, fxColor.getOpacity.toFloat)
  }

  /**
    * Returns the style for the svg shape of CellView
    *
    * @param color the color to set as background
    * @return the string representing the CellView background
    */
  private def getCellViewBackgroundStyle(color: Color): String = "-fx-background-color: #" + getHexDecimalColor(color)

  /**
    * Returns the Hex string representation of the color
    *
    * @param color the color of which to calculate the Hex representation
    * @return the Hex string representation
    */
  private def getHexDecimalColor(color: Color): String = Integer.toHexString(color.getRGB).substring(2)
}
