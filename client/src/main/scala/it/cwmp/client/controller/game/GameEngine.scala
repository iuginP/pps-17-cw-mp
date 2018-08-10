package it.cwmp.client.controller.game

import java.time.{Duration, Instant}
import java.util.Objects.requireNonNull

import it.cwmp.client.model.game.EvolutionStrategy
import it.cwmp.client.model.game.impl.{Cell, CellWorld, Tentacle}
import it.cwmp.utils.Logging

/**
  * Singleton of Game Engine
  *
  * The static game engine calculates the evolution of Game World assuming no more actions will be issued
  *
  * @author Enrico Siboni
  */
object GameEngine extends EvolutionStrategy[CellWorld, Duration] with Logging {

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

    val evolvedCellsAndCanAttack =
      oldToEvolve.characters.map(naturallyEvolveCell(_, elapsedTime)) // natural energy evolution
        .map(attackConsequencesOnAttackers(_, oldToEvolve.attacks)) // attackers reduction of energy, and not enough energy calculation

    var tempWorld = checkCellCanAttackAndRemoveNotPossibleAttacks(oldToEvolve, evolvedCellsAndCanAttack)

    // under attack reduction/increment of energy, and conquer management
    val evolvedCells = tempWorld.characters.map(attackConsequencesOnAttacked(_, tempWorld.attacks))

    tempWorld = cancelPendingAttacksOfConqueredCells(tempWorld, evolvedCells)
    tempWorld = updateAttacksToConqueredCells(tempWorld, tempWorld.characters)

    CellWorld(tempWorld.instant.plus(elapsedTime), tempWorld.characters, tempWorld.attacks)
  }

  /**
    * Naturally evolves cell according to elapsed time
    *
    * @param cell        the cell to evolve
    * @param elapsedTime the time elapsed
    * @return the evolved cell
    */
  private def naturallyEvolveCell(cell: Cell, elapsedTime: Duration): Cell = {
    if (Cell.isPassiveCell(cell)) {
      cell.evolve(elapsedTime, Cell.Passive.defaultEvolutionStrategy)
    } else {
      cell.evolve(elapsedTime)
    }
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
    for (tentacle <- tentacles if Cell.ownerAndPositionMatch(tentacle.from, cell) && cellAndCanAttack._2) {
      val addedTentacleLength = tentacle.length(oldWorldInstant.plus(elapsedTime)) - tentacle.length(oldWorldInstant)
      val attackerEnergyReduction = CellWorld.lengthToEnergyReductionStrategy(addedTentacleLength)

      if (cellAndCanAttack._1.energy <= attackerEnergyReduction) {
        cellAndCanAttack = (cellAndCanAttack._1, false) // if no more energy, the cell should not attack anymore
      } else {
        cellAndCanAttack = (cellAndCanAttack._1 -- attackerEnergyReduction, cellAndCanAttack._2)
      }
    }
    cellAndCanAttack
  }

  /**
    * A method that takes a world in input and a specification of which cells can attack an which not,
    * evolving accordingly the world
    *
    * @param world             the world to evolve
    * @param cellsAndCanAttack a sequence of pairs where every cell is said to be able to attack or not
    * @return the evolved world where cells that can attack are still attacking,
    *         cell that cannot attack (because of not enough energy), are refunded of the spent one
    *         and its attacks deleted from world
    */
  private def checkCellCanAttackAndRemoveNotPossibleAttacks(world: CellWorld,
                                                            cellsAndCanAttack: Seq[(Cell, Boolean)]): CellWorld = {
    var tempWorld = CellWorld(world.instant, cellsAndCanAttack.map(_._1), world.attacks)
    val cellsThatCannotAffordAttacking = cellsAndCanAttack.filterNot(_._2).map(_._1)
    for (poorCell <- cellsThatCannotAffordAttacking
         if world.attacks.map(_.from).exists(Cell.ownerAndPositionMatch(_, poorCell))) {
      // if cell cannot attack, refund it its energy and remove its last attack from world
      tempWorld = tempWorld -- world.attacks
        .filter(t => Cell.ownerAndPositionMatch(t.from, poorCell))
        .max(Tentacle.orderByLaunchInstant)
      // log.info(s"poorCell: $poorCell, removing ${world.attacks.filter(t => Cell.ownerAndPositionMatch(t.from, poorCell))
      // .max(Tentacle.orderByLaunchInstant)}, after: $tempWorld")
    }
    tempWorld
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
    val tentaclesToAttackedCell = allTentacles.filter(tentacle => Cell.ownerAndPositionMatch(tentacle.to, toReturnCell))
    val actualInstant = oldWorldInstant.plus(elapsedTime)

    for (tentacle <- tentaclesToAttackedCell) {
      val actualAttackDuration = tentacle.hasReachedDestinationFor(actualInstant)
      val oldAttackDuration = tentacle.hasReachedDestinationFor(oldWorldInstant)
      val addedAttackDuration = actualAttackDuration.minus(oldAttackDuration)

      val energyDelta = CellWorld.durationToEnergyConversionStrategy(addedAttackDuration)

      if (toReturnCell.owner == tentacle.from.owner) {
        // owner of the cell is attacking his own cells -> heals the cell
        toReturnCell = toReturnCell ++ energyDelta

      } else if (!Cell.isPassiveCell(toReturnCell)) {
        // attacking others cell -> damages the cell

        if (toReturnCell.energy - energyDelta <= 0) {
          // if cell reaches 0 energy is conquered

          toReturnCell = cellAfterConquer(toReturnCell, tentaclesToAttackedCell, actualInstant,
            GameConstants.CELL_ENERGY_WHEN_BORN + (energyDelta - toReturnCell.energy))
        } else {
          toReturnCell = toReturnCell -- energyDelta
        }
      } else {
        // attacking passive cell
        if (actualAttackDuration.toMillis >= GameConstants.MILLIS_TO_PASSIVE_CELL_CONQUER) {
          toReturnCell = cellAfterConquer(toReturnCell, tentaclesToAttackedCell, actualInstant, toReturnCell.energy)
        }
      }
    }
    toReturnCell
  }


  /**
    * Defines how to behave after a cell conquer
    *
    * @param cell                    the cell that has been conquered
    * @param tentaclesToAttackedCell the active tentacles attacking provided cell
    * @param conquerInstant          the instant when conquer occurred
    * @param newEnergy               the energy the cell should have
    * @return the new cell after conquer
    */
  private def cellAfterConquer(cell: Cell,
                               tentaclesToAttackedCell: Seq[Tentacle],
                               conquerInstant: Instant,
                               newEnergy: Double): Cell = {
    val firstAttackerOfCell = tentaclesToAttackedCell.max(Tentacle.orderByAttackDuration(conquerInstant)).from.owner
    Cell(firstAttackerOfCell, cell.position, newEnergy)
  }

  /**
    * A method that takes a world where to find if some cells that have been conquered had attacks ongoing
    * In that case those are cancelled
    *
    * @param world the world to evolve
    * @param cells the cells to use
    * @return a world where all attacks come from actual cells
    */
  private def cancelPendingAttacksOfConqueredCells(world: CellWorld,
                                                   cells: Seq[Cell]): CellWorld = {
    var tempWorld = CellWorld(world.instant, cells, world.attacks)
    for (tentacle <- tempWorld.attacks if !cells.exists(Cell.ownerAndPositionMatch(_, tentacle.from))) {
      tempWorld = CellWorld(world.instant, cells, tempWorld.attacks.filterNot(_ == tentacle))
    }
    tempWorld
  }

  /**
    * A method that updates all attacks that were directed to a cell that has been conquered
    *
    * @param world the world
    * @param cells the evolved cells
    * @return a world where all attacks directed to no more "existing" cells (intended as pair owner-position)
    *         will be redirected to cells in same position, updating tentacles
    */
  private def updateAttacksToConqueredCells(world: CellWorld,
                                            cells: Seq[Cell]): CellWorld = {

    var tempWorld = CellWorld(world.instant, cells, world.attacks)
    for (tentacle <- tempWorld.attacks if !cells.exists(Cell.ownerAndPositionMatch(_, tentacle.to))) {
      val destinationUpdate = cells.filter(_.position == tentacle.to.position).head
      val updatedTentacle = Tentacle(tentacle.from, destinationUpdate, tentacle.launchInstant)
      tempWorld = CellWorld(world.instant, cells, tempWorld.attacks.filterNot(_ == tentacle) :+ updatedTentacle)
    }
    tempWorld
  }
}
