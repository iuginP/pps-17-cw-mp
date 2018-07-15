package it.cwmp.client.model.game.impl

import java.time.Instant
import java.util.Objects._

import it.cwmp.client.model.game.World

/**
  * Default implementation of the snapshot of Game World
  *
  * @param instant    the time this world represents
  * @param characters the cells in the world
  * @param attacks    the tentacles in the world
  * @author Enrico Siboni
  */
case class CellWorld(instant: Instant, characters: Stream[Cell], attacks: Stream[Tentacle]) extends World[Instant, Cell, Tentacle] {
  requireNonNull(instant, "World instant must not be null")
  requireNonNull(characters, "Cells must not be null")
  requireNonNull(attacks, "Tentacles must not be null")
}

/**
  * Companion object
  */
object CellWorld {

  // TODO: Add converter to DDATA
}
