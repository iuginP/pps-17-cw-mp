package it.cwmp.client.view.game.model

import javafx.scene.paint.Color

/**
  * Classe che rappresenta una cella
  *
  * @author Davide Borficchia
  * @author Eugenio Pierfederici
  * @param center punto nel quale verrà disegnata la cella
  * @param size dimensione della cella
  */
case class Cell(center: Point, color: Color, size: Int = 20)
