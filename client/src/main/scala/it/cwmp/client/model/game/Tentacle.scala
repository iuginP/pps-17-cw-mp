package it.cwmp.client.model.game

import javafx.scene.paint.Color

/**
  * A trait that describes a Tentacle
  */
trait Tentacle {
  def startCell: Cell

  def arriveCell: Cell

  def launchTime: Long
}

/**
  * Companion object
  */
object Tentacle {

  def apply(startCell: Cell, arriveCell: Cell, launchTime: Long): Tentacle = TentacleDefault(startCell, arriveCell, launchTime)

  /**
    * Default implementation of Tentacle
    *
    * @param startCell  the cell where tentacle starts
    * @param arriveCell the cell where tentacle is directed
    * @param launchTime the time at which the tentacle was launched
    */
  private case class TentacleDefault(startCell: Cell, arriveCell: Cell, launchTime: Long) extends Tentacle

  /**
    * Tentacle length calculator
    *
    * @param tentacle the tentacle of wich to calculate the distance
    */
  implicit class TentacleLengthCalculator(tentacle: Tentacle) {

    def length(actualTime: Long)
              (tentacleSpeedStrategy: SizingStrategy[Long, Int] = defaultSpeedStrategy): Int =
      tentacleSpeedStrategy.sizeOf(actualTime - tentacle.launchTime)
  }

  /**
    * Default strategy for sizing the tentacle basing decision on time
    *
    * Every second a distance of 1 is traveled
    */
  val defaultSpeedStrategy = TentacleSpeedStrategyDefault()

  private case class TentacleSpeedStrategyDefault() extends SizingStrategy[Long, Int] {
    override def sizeOf(elapsedTimeInMillis: Long): Int = (elapsedTimeInMillis / 1000).toInt
  }

  /**
    * Default coloring strategy for tentacles
    *
    * Copies the color of starting cell
    */
  val defaultColoringStrategy = TentacleColoringStrategyDefault()

  private case class TentacleColoringStrategyDefault() extends ColoringStrategy[Tentacle, Color] {
    override def colorOf(thing: Tentacle): Color = Cell.defaultColoringStrategy.colorOf(thing.startCell)
  }

}