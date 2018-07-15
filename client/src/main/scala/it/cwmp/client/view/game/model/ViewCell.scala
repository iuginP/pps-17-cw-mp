package it.cwmp.client.view.game.model

import it.cwmp.client.view.game.Constants._
import java.awt.Color

import it.cwmp.client.model.game.impl.Point

/**
  * Classe che rappresenta una cella
  *
  * @author Davide Borficchia
  * @author Eugenio Pierfederici
  * @param center punto nel quale verrà disegnata la cella
  * @param size   dimensione della cella
  */
case class ViewCell(center: Point, color: Color = defaultColor, size: Int = cellSize)
