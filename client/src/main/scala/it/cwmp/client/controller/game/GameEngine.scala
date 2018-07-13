package it.cwmp.client.controller.game

import it.cwmp.client.model.game.{Cell, EvolutionStrategy, World}

/**
  * Game Engine singleton
  */
object GameEngine extends EvolutionStrategy[World, Long] {

  override def evolveAccordingTo(timeToEvolveTo: Long, actualWorld: World): World = {
    val elapsedTime = timeToEvolveTo - actualWorld.time
    val evolvedCells = actualWorld.cells.map(Cell.cellEvolutionStrategy.evolveAccordingTo(elapsedTime, _))
    World(timeToEvolveTo, evolvedCells, actualWorld.tentacles)
  }

}