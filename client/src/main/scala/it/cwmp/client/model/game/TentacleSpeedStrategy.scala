package it.cwmp.client.model.game

/**
  * A trait thata describes how fast is a tentacle on reaching it's destination
  */
trait TentacleSpeedStrategy {
  /**
    * Calculates the traveled distance
    *
    * @param elapsedTimeInMillis the time elapsed
    * @return the traveled distance to show
    */
  def timeToDistance(elapsedTimeInMillis: Long): Int

}

/**
  * Companion object
  */
object TentacleSpeedStrategy {

  /**
    * Default strategy for speed
    *
    * Every second a distance of 1 is traveled
    */
  val defaultSpeedStrategy = TentacleSpeedStrategyDefault()

  private case class TentacleSpeedStrategyDefault() extends TentacleSpeedStrategy {
    override def timeToDistance(elapsedTimeInMillis: Long): Int = (elapsedTimeInMillis / 1000).toInt
  }

}