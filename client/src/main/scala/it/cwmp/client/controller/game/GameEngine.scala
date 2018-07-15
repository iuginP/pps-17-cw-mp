package it.cwmp.client.controller.game

import it.cwmp.client.model.game.EvolutionStrategy
import it.cwmp.client.model.game.impl.CellWorld

/**
  * Game Engine singleton
  */
object GameEngine extends EvolutionStrategy[CellWorld, Long] {

  override def apply(timeToEvolveTo: Long, actualWorld: CellWorld): CellWorld = {
    actualWorld // TODO:
  }

}