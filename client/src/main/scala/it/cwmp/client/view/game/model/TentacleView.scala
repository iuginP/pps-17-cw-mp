package it.cwmp.client.view.game.model

import javafx.scene.paint.Color
import java.time.{Duration, Instant}

import it.cwmp.client.model.game.SizingStrategy
import it.cwmp.client.model.game.impl.{GeometricUtils, Point, Tentacle}
import it.cwmp.client.view.game.{ColoringStrategy, GameViewConstants}


/**
  * Tentacle View utilities
  *
  * @author Enrico Siboni
  */
object TentacleView {

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
    (_: Tentacle) => GameViewConstants.DEFAULT_TENTACLE_THICKNESS

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
    } else tentacle.to.position
  }
}
