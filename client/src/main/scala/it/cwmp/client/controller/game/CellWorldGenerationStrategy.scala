package it.cwmp.client.controller.game

import it.cwmp.client.GameMain
import it.cwmp.client.model.game.impl.CellWorld
import it.cwmp.model.Participant

case class CellWorldGenerationStrategy() extends GenerationStrategy[Seq[Participant], CellWorld] {

  /**
    * Generates the world starting from a participant list
    *
    * @param participants the participants to game
    * @return the generated world starting from participants
    */
  override def apply(participants: Seq[Participant]): CellWorld = GameMain.debugWorld

}
