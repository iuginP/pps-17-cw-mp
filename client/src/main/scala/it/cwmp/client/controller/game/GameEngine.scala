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

    val evolvedCellsAndCanAttack = oldToEvolve.characters.map(_.evolve(elapsedTime)) // natural energy evolution
      .map(attackConsequencesOnAttackers(_, oldToEvolve.attacks)) // attackers reduction of energy, and not enough energy calculation

    var tempWorld = CellWorld(oldWorldInstant, evolvedCellsAndCanAttack.map(_._1), oldToEvolve.attacks)
    for (cellAndCanAttack <- evolvedCellsAndCanAttack;
         tentacle <- oldToEvolve.attacks if !cellAndCanAttack._2 && Cell.ownerAndPositionMatch(tentacle.from, cellAndCanAttack._1)) {
      tempWorld = tempWorld -- tentacle // if cell cannot attack, refund it its energy and remove attack from world
    }

    // under attack reduction/increment of energy, and conquer management
    val evolvedCells = evolvedCellsAndCanAttack.map(_._1).map(attackConsequencesOnAttacked(_, tempWorld.attacks))

    tempWorld = CellWorld(oldWorldInstant, evolvedCells, tempWorld.attacks)

    // if a cell was conquered, its pending attacks should be cancelled
    for (tentacle <- tempWorld.attacks if !evolvedCells.exists(Cell.ownerAndPositionMatch(_, tentacle.from))) {
      tempWorld = CellWorld(oldWorldInstant, evolvedCells, tempWorld.attacks.filterNot(_ == tentacle))
    }

    // if a cell was conquered attacks directed to it, should be updated
    for (tentacle <- tempWorld.attacks if !evolvedCells.exists(Cell.ownerAndPositionMatch(_, tentacle.to))) {
      val destinationUpdate = evolvedCells.filter(_.position == tentacle.to.position).head
      val updatedTentacle = Tentacle(tentacle.from, destinationUpdate, tentacle.launchInstant)
      tempWorld = CellWorld(oldWorldInstant, evolvedCells, tempWorld.attacks.filterNot(_ == tentacle) :+ updatedTentacle)
    }

    CellWorld(tempWorld.instant.plus(elapsedTime), tempWorld.characters, tempWorld.attacks)
  }

  /**
    * Calculation of attack consequences on attacked cells
    *
    * If attacked cell is property of attacker the attack heals the cell
    * If attacked cell is property of other player the attack damages the cell
    *
    * @return the updated cell
    */
  private def attackConsequencesOnAttacked(cell: Cell, allTentacles: Seq[Tentacle])
                                          (implicit oldWorldInstant: Instant, elapsedTime: Duration): Cell = {

    var toReturnCell = cell
    for (tentacle <- allTentacles if Cell.ownerAndPositionMatch(tentacle.to, toReturnCell); // all tentacles arriving to cell
         actualAttackDuration = tentacle.hasReachedDestinationFor(oldWorldInstant.plus(elapsedTime));
         oldAttackDuration = tentacle.hasReachedDestinationFor(oldWorldInstant);
         addedAttackDuration = actualAttackDuration.minus(oldAttackDuration)) {

      val energyDelta = CellWorld.durationToEnergyConversionStrategy(addedAttackDuration)

      // owner of the cell is attacking his own cells -> heals the cell
      if (cell.owner == tentacle.from.owner) toReturnCell = toReturnCell ++ energyDelta
      // attacking others cell -> damages the cell
      else {
        if (toReturnCell.energy - energyDelta <= 0) { // if cell reaches 0 energy is conquered
          toReturnCell = cellAfterConquer(cell, allTentacles, energyDelta - toReturnCell.energy) // TODO: pensare a come le celle conquistate dovrebbero cancellare i propri tentacoli... in realtà i tentacoli rimangono col vecchio proprietario, quindi potrei capire quali tentacoli rimuovere senza far ritornare nulla
        } else toReturnCell = toReturnCell -- energyDelta
      }
    }
    toReturnCell
  }

  /**
    * Defines how to behave after a cell conquer
    *
    * @param cell         the cell that has been conquered
    * @param allTentacles the active tentacles
    * @return
    */
  private def cellAfterConquer(cell: Cell, allTentacles: Seq[Tentacle], maturedEnergy: Double): Cell = {
    val firstAttackerOfCell = allTentacles.min((x: Tentacle, y: Tentacle) => x.launchInstant.compareTo(y.launchInstant)).from.owner
    Cell(firstAttackerOfCell, cell.position, Cell.whenBornEnergy + maturedEnergy)
  }

  /**
    * Calculation of attack consequences on attackers
    *
    * Each attacker loses part of its energy while a tentacle is travelling towards another cell
    *
    * @return the pair composed by the cell and a boolean indicating if that call can still attack or not (energy finished)
    */
  private def attackConsequencesOnAttackers(cell: Cell, tentacles: Seq[Tentacle])
                                           (implicit oldWorldInstant: Instant, elapsedTime: Duration): (Cell, Boolean) = {

    var cellAndCanAttack = (cell, true)

    // all tentacles leaving from cell, checking if can still attack
    for (tentacle <- tentacles if Cell.ownerAndPositionMatch(tentacle.from, cell) && cellAndCanAttack._2;
         addedTentacleLength = tentacle.length(oldWorldInstant.plus(elapsedTime)) - tentacle.length(oldWorldInstant);
         attackerEnergyReduction = CellWorld.lengthToEnergyReductionStrategy(addedTentacleLength)) {

      if (cellAndCanAttack._1.energy <= attackerEnergyReduction)
        cellAndCanAttack = (cellAndCanAttack._1, false) // if no more energy, the cell should not attack anymore
      else
        cellAndCanAttack = (cellAndCanAttack._1 -- attackerEnergyReduction, cellAndCanAttack._2)
    }
    cellAndCanAttack
  }
}