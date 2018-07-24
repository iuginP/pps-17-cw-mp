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
}
