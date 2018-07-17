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

    implicit val elapsedTimeImplicit: Duration = elapsedTime
    implicit val oldWorldInstant: Instant = oldToEvolve.instant

    val evolvedCells =
      oldToEvolve.characters
        .map(_.evolve(elapsedTime)) // natural energy evolution
        .map(attackConsequencesOnAttackers(_, oldToEvolve.attacks)) // attackers reduction of energy
        .map(attackConsequencesOnAttacked(_, oldToEvolve.attacks)) // under attack reduction/increment of energy

    CellWorld(oldToEvolve.instant.plus(elapsedTime), evolvedCells, oldToEvolve.attacks)
  }

  /**
    * Calculation of attack consequences on attacked cells
    *
    * If attacked cell is property of attacker the attack heals the cell
    * If attacked cell is property of other player the attack damages the cell
    *
    * @return the updated cell
    */
  private def attackConsequencesOnAttacked(cell: Cell, tentacles: Seq[Tentacle])
                                          (implicit oldWorldInstant: Instant, elapsedTime: Duration): Cell = {

    var toReturnCell = cell
    for (tentacle <- tentacles if Cell.ownerAndPositionMatch(tentacle.to, toReturnCell); // all tentacles arriving to cell
         actualAttackDuration = tentacle.hasReachedDestinationFor(oldWorldInstant.plus(elapsedTime));
         oldAttackDuration = tentacle.hasReachedDestinationFor(oldWorldInstant);
         addedAttackDuration = actualAttackDuration.minus(oldAttackDuration)) {

      val energyDelta = CellWorld.durationToEnergyConversionStrategy(addedAttackDuration)

      // owner of the cell is attacking his own cells -> heals the cell
      if (cell.owner == tentacle.from.owner) toReturnCell = toReturnCell ++ energyDelta
      // attacking others cell -> damages the cell
      else {
        if (toReturnCell.energy - energyDelta <= 0) {
          toReturnCell = cellAfterConquer(cell, tentacles, energyDelta - toReturnCell.energy)
        } else toReturnCell = toReturnCell -- energyDelta
      }
    }
    toReturnCell
  }

  /**
    * Defines how to behave after a cell conquer
    *
    * @param cell      the cell that has been conquered
    * @param tentacles the active tentacles
    * @return
    */
  private def cellAfterConquer(cell: Cell, tentacles: Seq[Tentacle], maturedEnergy: Double): Cell = {
    val firstAttackerOfCell = tentacles.min((x: Tentacle, y: Tentacle) => x.launchInstant.compareTo(y.launchInstant)).from.owner
    Cell(firstAttackerOfCell, cell.position, Cell.whenBornEnergy + maturedEnergy)
  }

  /**
    * Calculation of attack consequences on attackers
    *
    * Each attacker loses part of its energy while a tentacle is travelling towards another cell
    *
    * @return the stream of attacker cells modified with its consequences
    */
  private def attackConsequencesOnAttackers(cell: Cell, tentacles: Seq[Tentacle])
                                           (implicit oldWorldInstant: Instant, elapsedTime: Duration): Cell = {

    var toReturnCell = cell
    for (tentacle <- tentacles if Cell.ownerAndPositionMatch(tentacle.from, cell); // all tentacles leaving from cell
         addedTentacleLength = tentacle.length(oldWorldInstant.plus(elapsedTime)) - tentacle.length(oldWorldInstant);
         attackerEnergyReduction = CellWorld.lengthToEnergyReductionStrategy(addedTentacleLength)) {
      toReturnCell = toReturnCell -- attackerEnergyReduction
    } // TODO: delete tentacle attack if attacker has not enough energy
    toReturnCell
  }
}