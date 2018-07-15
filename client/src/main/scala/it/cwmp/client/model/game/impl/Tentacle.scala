package it.cwmp.client.model.game.impl

import java.awt.Color
import java.time.temporal.ChronoUnit
import java.time.{Duration, Instant}

import it.cwmp.client.model.game.{Attack, ColoringStrategy, SizingStrategy}

/**
  * Default implementation of cell attack
  *
  * @param from          the cell where tentacle starts
  * @param to            the cell where tentacle is directed
  * @param launchInstant the time at which the tentacle was launched
  * @author Enrico Siboni
  */
case class Tentacle(from: Cell,
                    to: Cell,
                    launchInstant: Instant)
  extends Attack[Cell, Cell, Instant]

/**
  * Companion object
  */
object Tentacle {

  private val TENTACLE_MOVES_EVERY_MILLIS = 100

  /**
    * Tentacle length calculator
    *
    * @param tentacle the tentacle of which to calculate the distance traveled
    */
  implicit class TentacleLengthCalculator(tentacle: Tentacle) {

    /**
      * Returns the length of the tentacle based on launch time, actualTime provided and speed of the tentacle
      *
      * @param actualInstant         actual world time
      * @param tentacleSpeedStrategy the strategy that decides how fast is the tentacle on reaching destination
      * @return the tentacle length
      */
    def length(actualInstant: Instant)
              (tentacleSpeedStrategy: SizingStrategy[Duration, Long] = lengthStrategy): Long = {

      val distanceBetweenCells = Cell.distance(tentacle.from, tentacle.to)
      val tentacleShouldHaveLength = tentacleSpeedStrategy(Duration.between(tentacle.launchInstant, actualInstant))

      if (tentacleShouldHaveLength > distanceBetweenCells) distanceBetweenCells else tentacleShouldHaveLength
    }
  }

  /**
    * Default strategy for sizing the tentacle basing decision on time
    *
    * Every [[TENTACLE_MOVES_EVERY_MILLIS]] tentacle gains 1 space towards enemy
    */
  val lengthStrategy: SizingStrategy[Duration, Long] =
    (elapsedTime: Duration) => elapsedTime.get(ChronoUnit.MILLIS) / TENTACLE_MOVES_EVERY_MILLIS

}