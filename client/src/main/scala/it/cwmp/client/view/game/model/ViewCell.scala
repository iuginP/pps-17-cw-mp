package it.cwmp.client.view.game.model

import it.cwmp.client.model.game.Point
import it.cwmp.client.view.game.Constants._
import javafx.scene.paint.Color

/**
  * Classe che rappresenta una cella
  *
  * @author Davide Borficchia
  * @author Eugenio Pierfederici
  * @param center punto nel quale verrà disegnata la cella
  * @param size   dimensione della cella
  */
case class ViewCell(center: Point, size: Int = cellSize, color: Color = defaultColor)
