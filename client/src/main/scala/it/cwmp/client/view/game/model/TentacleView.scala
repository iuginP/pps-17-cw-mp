package it.cwmp.client.view.game.model

import java.awt.Color

import it.cwmp.client.model.game.impl.Tentacle
import it.cwmp.client.view.game.ColoringStrategy


/**
  * Tentacle View utilities
  */
object TentacleView {

  /**
    * Default coloring strategy for tentacles
    *
    * Copies the color of starting cell
    */
  val coloringStrategy: ColoringStrategy[Tentacle, Color] =
    (tentacle: Tentacle) => CellView.coloringStrategy(tentacle.from)

}
