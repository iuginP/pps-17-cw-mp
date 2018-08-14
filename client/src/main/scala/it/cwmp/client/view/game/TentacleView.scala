package it.cwmp.client.view.game

import java.time.{Duration, Instant}

import it.cwmp.client.model.game.SizingStrategy
import it.cwmp.client.model.game.impl.{Point, Tentacle}
import it.cwmp.client.utils.GeometricUtils
import it.cwmp.client.view.game.drawing.ColoringStrategy
import javafx.scene.paint.Color

import scala.language.implicitConversions

/**
  * A class representing the View counterpart of Tentacle
  *
  * @param startPoint   the starting point for this tentacle View
  * @param reachedPoint the arrive point for this tentacle View
  * @param color        the color of this tentacle
  * @author Enrico Siboni
  */
case class TentacleView(startPoint: Point, reachedPoint: Point, color: Color, thickness: Double)

/**
  * Companion Object
  *
  * @author Enrico Siboni
  */
object TentacleView {

  /**
    * Provides default value for tentacle thickness
    */
  val TENTACLE_DEFAULT_THICKNESS = 3d

  /**
    * Provides tentacle default color opacity
    */
  val TENTACLE_COLOR_OPACITY = 0.6

  /**
    * Conversion from Tentacle to TentacleView
    *
    * @param tentacle      the tentacle to convert
    * @param actualInstant the instant of the world
    * @return the TentacleView corresponding to the give Tentacle
    */
  def tentacleToView(tentacle: Tentacle, actualInstant: Instant): TentacleView =
    TentacleView(tentacle.from.position, reachedPoint(tentacle, actualInstant), coloringStrategy(tentacle), thicknessStrategy(tentacle))

  /**
    * Default coloring strategy for tentacles
    *
    * Copies the color of starting cell
    */
  val coloringStrategy: ColoringStrategy[Tentacle, Color] =
    (tentacle: Tentacle) => CellView.coloringStrategy(tentacle.from)
      .deriveColor(0, 1, 1, TENTACLE_COLOR_OPACITY)

  /**
    * Default tentacle thickness strategy
    *
    * Returns always same thickness
    */
  val thicknessStrategy: SizingStrategy[Tentacle, Double] =
    (_: Tentacle) => TENTACLE_DEFAULT_THICKNESS

  /**
    * A method returning the Point reached actually by the tentacle
    *
    * @param tentacle      the tentacle to calculate the reached point
    * @param actualInstant the instant to draw
    * @return the point that the tentacle has reached going towards enemy cell
    */
  def reachedPoint(tentacle: Tentacle, actualInstant: Instant): Point = {
    val tentacleActualLength = tentacle.length(actualInstant)

    if (tentacleActualLength == 0) {
      tentacle.from.position
    } else if (tentacle.hasReachedDestinationFor(actualInstant) == Duration.ZERO) {
      val attackerPosition = tentacle.from.position
      val deltaXYFromAttackerPosition = GeometricUtils.deltaXYFromFirstPoint(attackerPosition, tentacle.to.position, tentacleActualLength)
      Point(attackerPosition.x + deltaXYFromAttackerPosition._1.toInt,
        attackerPosition.y + deltaXYFromAttackerPosition._2.toInt)
    } else {
      tentacle.to.position
    }
  }
}
