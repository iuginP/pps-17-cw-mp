package it.cwmp.client.controller.game

import java.time.Duration
import java.util.Objects.requireNonNull

import it.cwmp.client.model.game.EvolutionStrategy
import it.cwmp.client.model.game.impl.CellWorld

/**
  * Game Engine singleton
  *
  * @author Enrico Siboni
  */
object GameEngine extends EvolutionStrategy[CellWorld, Duration] {

  override def apply(elapsedTime: Duration, toEvolve: CellWorld): CellWorld = {
    requireNonNull(elapsedTime, "Duration must not be null")
    requireNonNull(toEvolve, "World to evolve must not be null")

    // TODO:  implement evolution of world
    toEvolve
  }

}