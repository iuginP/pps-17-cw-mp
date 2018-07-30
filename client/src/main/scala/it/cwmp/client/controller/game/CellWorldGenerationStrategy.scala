package it.cwmp.client.controller.game

import java.time.Instant

import it.cwmp.client.model.game.impl.{Cell, CellWorld}
import it.cwmp.model.Participant

/**
  * A class implementing a CellWorld generation strategy
  *
  * @author Enrico Siboni
  */
case class CellWorldGenerationStrategy(worldHeight: Int, worldWidth: Int) extends GenerationStrategy[Seq[Participant], CellWorld] {

  /**
    * Generates the world starting from a participant list
    *
    * @param participants the participants to game
    * @return the generated world starting from participants
    */
  override def apply(participants: Seq[Participant]): CellWorld =
    CellWorld(Instant.now(), getParticipantCells(participants), Seq())

  /**
    * A method to position participant cells into game world
    *
    * @param participants the participants to assign a cell into the world
    * @return the cells assigned to participants
    */
  private def getParticipantCells(participants: Seq[Participant]): Seq[Cell] = ???
}
