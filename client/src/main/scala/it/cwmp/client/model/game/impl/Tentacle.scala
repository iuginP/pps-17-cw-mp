package it.cwmp.client.model.game.impl

import java.time.{Duration, Instant}
import java.util.Objects._

import it.cwmp.client.model.game.{Attack, SizingStrategy}

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
  extends Attack[Cell, Cell, Instant] {

  requireNonNull(from, "From cell must be not null")
  requireNonNull(to, "To cell must be not null")
  requireNonNull(launchInstant, "Launch instant must be not null")
}

/**
  * Companion object
  */
object Tentacle {

  /**
    * Amount of time expressed in milliseconds that will be converted to a movement step of the tentacle
    */
  val TIME_TO_MOVEMENT_CONVERSION_RATE = 100

  /**
    * Default strategy for sizing the tentacle basing decision on time
    *
    * Every [[TIME_TO_MOVEMENT_CONVERSION_RATE]] tentacle gains 1 space towards enemy
    */
  val defaultTimeToLengthStrategy: SizingStrategy[Duration, Long] =
    (elapsedTime: Duration) => elapsedTime.toMillis / TIME_TO_MOVEMENT_CONVERSION_RATE

  /**
    * Tentacle length calculator
    *
    * @param tentacle the tentacle of which to calculate the distance traveled
    */
  implicit class TentacleLengthCalculator(tentacle: Tentacle) {

    /**
      * Returns the length of the tentacle based on launch time, actualTime provided and speed of the tentacle
      *
      * @param actualInstant        actual world time
      * @param timeToLengthStrategy the strategy that decides how fast is the tentacle on reaching destination
      * @return the tentacle length
      */
    def length(actualInstant: Instant,
               timeToLengthStrategy: SizingStrategy[Duration, Long] = defaultTimeToLengthStrategy): Long = {

      val distanceBetweenCells = Cell.distance(tentacle.from, tentacle.to)
      val tentacleShouldHaveLength = timeToLengthStrategy(Duration.between(tentacle.launchInstant, actualInstant))

      if (tentacleShouldHaveLength > distanceBetweenCells) distanceBetweenCells else tentacleShouldHaveLength
    }
  }

}