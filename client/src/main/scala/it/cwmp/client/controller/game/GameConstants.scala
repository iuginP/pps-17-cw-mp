package it.cwmp.client.controller.game

/**
  * An object where to put constants about the game behaviour
  *
  * @author Enrico Siboni
  */
object GameConstants {

  /**
    * ==Cell==
    *
    * The energy that a cell has when it's born
    */
  val CELL_ENERGY_WHEN_BORN = 20d

  /**
    * ==Cell==
    *
    * Coverts this amount of milliseconds to an energy unit for Cell
    */
  val MILLIS_TO_ENERGY_CONVERSION_RATE = 1000d

  /**
    * ==Passive Cell==
    *
    * The energy that a passive cell has when it's born
    */
  val PASSIVE_CELL_ENERGY_WHEN_BORN = 40d

  /**
    * Time needed to conquer a passive cell (expressed in milliseconds)
    */
  val MILLIS_TO_PASSIVE_CELL_CONQUER = 3000

  /**
    * ==Tentacle==
    *
    * Amount of time expressed in milliseconds that will be converted to a movement step of the tentacle
    */
  val MILLIS_TO_MOVEMENT_CONVERSION_RATE = 100

  /**
    * ==CellWorld==
    *
    * Amount of length that will be converted in one energy reduction in attacker cell
    */
  val LENGTH_TO_ENERGY_REDUCTION_RATE = 2d

  /**
    * ==CellWorld==
    *
    * Amount of time expressed in milliseconds that will be converted in 1 energy reduction on under attack character
    */
  val ATTACK_DURATION_TO_ENERGY_REDUCTION_RATE = 1000d

  /**
    * The number of passive cells in generated world
    */
  val PASSIVE_CELLS_NUMBER = 5

  /**
    * The minimum time that will last between two clients synchronizations in millis
    */
  val MIN_TIME_BETWEEN_CLIENT_SYNCHRONIZATION = 4000

  /**
    * The maximum time that will last between two client synchronizations in millis
    */
  val MAX_TIME_BETWEEN_CLIENT_SYNCHRONIZATION = 8000
}
