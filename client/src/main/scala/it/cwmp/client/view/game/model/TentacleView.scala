package it.cwmp.client.view.game.model

import java.awt.Color
import java.time.{Duration, Instant}

import it.cwmp.client.model.game.impl.{Point, Tentacle}
import it.cwmp.client.view.game.ColoringStrategy


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
    * A method returning the Point reached actually by the tentacle
    *
    * @param tentacle      the tentacle to calculate the reached point
    * @param actualInstant the instant to draw
    * @return the point that the tentacle has reached going towards enemy cell
    */
  def reachedPoint(tentacle: Tentacle, actualInstant: Instant): Point = {
    if (tentacle.hasReachedDestinationFor(actualInstant) == Duration.ZERO) {
      val tentacleActualLength = tentacle.length(actualInstant)
      Point(1, 1) // TODO: finish implementig this method with GeometricUtils to provide the point that the tentacle has reached
    } else tentacle.to.position
  }
}
