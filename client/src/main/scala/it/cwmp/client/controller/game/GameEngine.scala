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
    * @param toEvolve    the world to evolve
    * @param elapsedTime the amount of time that should be reflected in changes in "toEvolve" world
    * @return the evolved world
    */
  override def apply(toEvolve: CellWorld, elapsedTime: Duration): CellWorld = {
    evolveWithStrategies(toEvolve, elapsedTime)(Cell.evolutionStrategy)
  }


  private def evolveWithStrategies(toEvolve: CellWorld,
                                   elapsedTime: Duration)
                                  (cellEvolutionStrategy: EvolutionStrategy[Cell, Duration]): CellWorld = {

    requireNonNull(toEvolve, "World to evolve must not be null")
    requireNonNull(elapsedTime, "Elapsed time must not be null")

    val evolvedCharacters = evolveCharacters(toEvolve.characters, elapsedTime)(cellEvolutionStrategy)
    for (character <- evolvedCharacters;
         attack <- toEvolve.attacks if attack.from == character || attack.to == character) {
      // TODO:
    }
    toEvolve
  }

  /**
    * Evolves the characters according to elapsed time with given evolutionStrategy
    *
    * @param characters            the characters to evolve
    * @param elapsedTime           the elapsedTime to consider
    * @param cellEvolutionStrategy the strategy to use evolving characters
    * @return
    */
  private def evolveCharacters(characters: Seq[Cell], elapsedTime: Duration)
                              (cellEvolutionStrategy: EvolutionStrategy[Cell, Duration]): Stream[Cell] = {
    characters.toStream.map(cellEvolutionStrategy(_, elapsedTime))
  }
}