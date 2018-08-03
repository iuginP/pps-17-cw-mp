package it.cwmp.client.model.game.impl

import java.time.{Duration, Instant}
import java.util.Objects.requireNonNull

import it.cwmp.client.controller.game.GameConstants.MILLIS_TO_MOVEMENT_CONVERSION_RATE
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
  require(from != to, "A tentacle cannot be launched from and to same cell")
}

/**
  * Companion object
  */
object Tentacle {

  /**
    * Default strategy for sizing the tentacle basing decision on time
    *
    * Every [[it.cwmp.client.controller.game.GameConstants.MILLIS_TO_MOVEMENT_CONVERSION_RATE]] tentacle gains 1 space towards enemy
    */
  val defaultMillisToLengthStrategy: SizingStrategy[Duration, Long] =
    (elapsedTime: Duration) => elapsedTime.toMillis / MILLIS_TO_MOVEMENT_CONVERSION_RATE

  /**
    * A class to manipulate tentacle properties
    *
    * @param tentacle the tentacle to work on
    */
  implicit class TentacleManipulation(tentacle: Tentacle) {

    private val distanceBetweenCells = Cell.distance(tentacle.from, tentacle.to)

    /**
      * Returns the length of the tentacle based on launch time, actualTime provided and speed of the tentacle
      *
      * @param actualInstant        actual world time
      * @param timeToLengthStrategy the strategy that decides how fast is the tentacle on reaching destination
      * @return the tentacle length
      */
    def length(actualInstant: Instant,
               timeToLengthStrategy: SizingStrategy[Duration, Long] = defaultMillisToLengthStrategy): Long = {
      checkTentacleManipulationParameters(actualInstant, timeToLengthStrategy)

      val tentacleShouldHaveLength = timeToLengthStrategy(Duration.between(tentacle.launchInstant, actualInstant))
      if (tentacleShouldHaveLength > distanceBetweenCells) distanceBetweenCells else tentacleShouldHaveLength
    }

    /**
      * Return the amount of time the tentacle has been attacking the destination cell
      *
      * @param actualInstant        the actual instant
      * @param timeToLengthStrategy the strategy that decides how fast is the tentacle on reaching destination
      * @return the time spent really attacking the destination cell
      */
    def hasReachedDestinationFor(actualInstant: Instant,
                                 timeToLengthStrategy: SizingStrategy[Duration, Long] = defaultMillisToLengthStrategy): Duration = {
      /**
        * @return the speed factor extracted from duration and space traveled
        */
      def extractSpeedFactor(duration: Duration, space: Long): Long = duration.toMillis / space

      checkTentacleManipulationParameters(actualInstant, timeToLengthStrategy)

      if (Cell.distance(tentacle.from, tentacle.to) == length(actualInstant, timeToLengthStrategy)) {
        val fromLaunchDuration = Duration.between(tentacle.launchInstant, actualInstant)
        val tentacleShouldHaveLength = timeToLengthStrategy(fromLaunchDuration)
        val timeToLengthFactor = extractSpeedFactor(fromLaunchDuration, tentacleShouldHaveLength)

        val exceedingLength = tentacleShouldHaveLength - distanceBetweenCells
        Duration.ofMillis(exceedingLength * timeToLengthFactor)
      } else Duration.ZERO
    }

    /**
      * Checks actualInstant not before tentacle launch instant and parameters not null
      */
    private def checkTentacleManipulationParameters(actualInstant: Instant, strategy: SizingStrategy[_, _]): Unit = {
      requireNonNull(actualInstant, "Actual instant should not be null")
      requireNonNull(strategy, "Passed strategy cannot be null")
      require(!actualInstant.isBefore(tentacle.launchInstant), "Actual instant cannot be before tentacle launch")
    }
  }

  /**
    * An ordering for tentacles based on launchInstant
    */
  val orderByLaunchInstant: Ordering[Tentacle] = (x: Tentacle, y: Tentacle) => x.launchInstant.compareTo(y.launchInstant)

}
