package it.cwmp.client.controller.game

import java.time.{Duration, Instant}
import java.util.Objects.requireNonNull

import it.cwmp.client.model.game.EvolutionStrategy
import it.cwmp.client.model.game.impl.{Cell, CellWorld, Tentacle}

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
    requireNonNull(oldToEvolve, "World to evolve must not be null")
    requireNonNull(elapsedTime, "Elapsed time must not be null")

    val naturallyEvolvedCells = oldToEvolve.characters.map(_.evolve(elapsedTime))

    attackConsequencesOnAttackers(oldToEvolve.instant, elapsedTime, naturallyEvolvedCells, oldToEvolve.attacks)

    oldToEvolve
  }


  private def attackConsequencesOnAttacked(oldWorldInstant: Instant,
                                           elapsedTime: Duration,
                                           cells: Seq[Cell],
                                           tentacles: Seq[Tentacle]): Seq[Cell] = {
//    for (cell <- cells;
//         tentacle <- tentacles if Cell.ownerAndPositionMatch(tentacle.to, cell); // all tentacles arriving to cell
//      tentacleLength = tentacle.length(old)
//    )
//      yield cell
    Seq()
  }


  private def attackConsequencesOnAttackers(oldWorldInstant: Instant,
                                            elapsedTime: Duration,
                                            cells: Seq[Cell],
                                            tentacles: Seq[Tentacle]): Seq[Cell] = {
    for (cell <- cells;
         tentacle <- tentacles if Cell.ownerAndPositionMatch(tentacle.from, cell); // all tentacles leaving from cell
         addedTentacleLength = tentacle.length(oldWorldInstant.plus(elapsedTime)) - tentacle.length(oldWorldInstant);
         attackerEnergyReduction = CellWorld.lengthToEnergyReductionStrategy(addedTentacleLength))
      yield cell -- attackerEnergyReduction
  }
}