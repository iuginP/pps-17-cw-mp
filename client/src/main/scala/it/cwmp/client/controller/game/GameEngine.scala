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

    // TODO: try to make it a stream
    val naturallyEvolvedCells = oldToEvolve.characters.map(_.evolve(elapsedTime)).toStream
    val cellsWithAttackersConsequences = attackConsequencesOnAttackers(oldToEvolve.instant, elapsedTime, naturallyEvolvedCells, oldToEvolve.attacks)
    val cellsWithAllConsequences = attackConsequencesOnAttacked(oldToEvolve.instant, elapsedTime, cellsWithAttackersConsequences, oldToEvolve.attacks)
    CellWorld(oldToEvolve.instant.plus(elapsedTime), cellsWithAllConsequences, oldToEvolve.attacks)
  }

  /**
    * Calculation of attack consequences on attacked cells
    *
    * If attacked cells is property of attacker the attack heals the cell
    * If attacked cell is property of other player the attack damages the cell
    *
    * @return the stream of modified cells
    */
  private def attackConsequencesOnAttacked(oldWorldInstant: Instant,
                                           elapsedTime: Duration,
                                           cells: Stream[Cell],
                                           tentacles: Seq[Tentacle]): Stream[Cell] = {
    for (cell <- cells;
         tentacle <- tentacles if Cell.ownerAndPositionMatch(tentacle.to, cell); // all tentacles arriving to cell
         actualAttackDuration = tentacle.hasReachedDestinationFor(oldWorldInstant.plus(elapsedTime));
         oldAttackDuration = tentacle.hasReachedDestinationFor(oldWorldInstant);
         addedAttackDuration = actualAttackDuration.minus(oldAttackDuration)
    ) yield {
      val energyDelta = CellWorld.durationToEnergyConversionStrategy(addedAttackDuration)
      if (cell.owner == tentacle.from.owner) cell ++ energyDelta // owner of the cell is attacking his own cells -> heals the cell
      else cell -- energyDelta // attacking others cell -> damages the cell
    }
  }

  /**
    * Calculation of attack consequences on attackers
    *
    * Each attacker loses part of its energy while a tentacle is travelling towards another cell
    * @return
    */
  private def attackConsequencesOnAttackers(oldWorldInstant: Instant,
                                            elapsedTime: Duration,
                                            cells: Stream[Cell],
                                            tentacles: Seq[Tentacle]): Stream[Cell] = {
    for (cell <- cells;
         tentacle <- tentacles if Cell.ownerAndPositionMatch(tentacle.from, cell); // all tentacles leaving from cell
         addedTentacleLength = tentacle.length(oldWorldInstant.plus(elapsedTime)) - tentacle.length(oldWorldInstant);
         attackerEnergyReduction = CellWorld.lengthToEnergyReductionStrategy(addedTentacleLength))
      yield cell -- attackerEnergyReduction
  }
}