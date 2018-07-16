package it.cwmp.client.controller.game

import java.time.Duration
import java.util.Objects.requireNonNull

import it.cwmp.client.model.game.EvolutionStrategy
import it.cwmp.client.model.game.impl.{Cell, CellWorld}

/**
  * Singleton of Static Game Engine
  *
  * The static game engine calculates the evolution of Game World assuming no more actions will be issued
  *
  * @author Enrico Siboni
  */
object GameEngine extends EvolutionStrategy[CellWorld, Duration] {

  /**
    * Evolves the world using default evolution strategies
    *
    * @param oldToEvolve the world to evolve
    * @param elapsedTime the amount of time that should be reflected in changes in "toEvolve" world
    * @return the evolved world
    */
  override def apply(oldToEvolve: CellWorld, elapsedTime: Duration): CellWorld = {
    evolveWithStrategies(oldToEvolve, elapsedTime)(Cell.defaultEvolutionStrategy)
  }


  private def evolveWithStrategies(oldToEvolve: CellWorld,
                                   elapsedTime: Duration)
                                  (cellEvolutionStrategy: EvolutionStrategy[Cell, Duration]): CellWorld = {

    requireNonNull(oldToEvolve, "World to evolve must not be null")
    requireNonNull(elapsedTime, "Elapsed time must not be null")

    val evolvedCells = oldToEvolve.characters.map(_.evolve(elapsedTime))

    for (cell <- evolvedCells;
         tentacle <- oldToEvolve.attacks if Cell.ownerAndPositionMatch(tentacle.from, cell)) {

      val addedTentacleLength = tentacle.length(oldToEvolve.instant.plus(elapsedTime)) - tentacle.length(oldToEvolve.instant)
      val attackerEnergyReduction = CellWorld.lengthToEnergyReductionStrategy(addedTentacleLength)

    }
    oldToEvolve
  }
}