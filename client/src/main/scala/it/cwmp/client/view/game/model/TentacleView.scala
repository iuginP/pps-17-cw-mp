package it.cwmp.client.view.game.model

import java.time.{Duration, Instant}

import it.cwmp.client.model.game.impl.{Point, Tentacle}
import it.cwmp.client.model.game.{GeometricUtils, SizingStrategy}
import it.cwmp.client.view.game.ColoringStrategy
import javafx.scene.paint.Color

import scala.language.implicitConversions

/**
  * A class representing the View counterpart of Tentacle
  *
  * @param startPoint  the starting point for this tentacle
  * @param arrivePoint the arrive point for thi tentacle
  * @param color       the color of this tetacle
  * @author Enrico Siboni
  */
case class TentacleView(startPoint: Point, arrivePoint: Point, color: Color, thickness: Double)

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

  implicit def tentacleToTentacleView(tentacle: Tentacle): TentacleView =
    TentacleView(tentacle.from.position, tentacle.to.position, coloringStrategy(tentacle), thicknessStrategy(tentacle))

  /**
    * Default coloring strategy for tentacles
    *
    * Copies the color of starting cell
    */
  val coloringStrategy: ColoringStrategy[Tentacle, Color] =
    (tentacle: Tentacle) => CellView.coloringStrategy(tentacle.from)

  /**
    * Default tentacle thickness strategy
    *
    * Returns always same thickness
    */
  val thicknessStrategy: SizingStrategy[Tentacle, Double] = // TODO: make thickness vary with attackers cell energy
    (_: Tentacle) => TENTACLE_DEFAULT_THICKNESS

  /**
    * A method returning the Point reached actually by the tentacle
    *
    * @param tentacle      the tentacle to calculate the reached point
    * @param actualInstant the instant to draw
    * @return the point that the tentacle has reached going towards enemy cell
    */
  def reachedPoint(tentacle: Tentacle, actualInstant: Instant): Point = {
    if (tentacle.hasReachedDestinationFor(actualInstant) == Duration.ZERO) {
      val tentacleActualLength = tentacle.length(actualInstant)
      val attackerPosition = tentacle.from.position
      val deltaXYFromAttackerPosition = GeometricUtils.deltaXYFromFirstPoint(attackerPosition, tentacle.to.position, tentacleActualLength)
      Point(attackerPosition.x + deltaXYFromAttackerPosition._1.toInt,
        attackerPosition.y + deltaXYFromAttackerPosition._2.toInt)
    } else {
      tentacle.to.position
    }
  }
}
