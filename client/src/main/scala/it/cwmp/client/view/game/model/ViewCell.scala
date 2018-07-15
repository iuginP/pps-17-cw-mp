package it.cwmp.client.view.game.model

import it.cwmp.client.model.game.{Cell, Point}
import it.cwmp.client.view.game.Constants._
import java.awt.Color

import com.github.tkqubo.colorHash.ColorHash
import it.cwmp.model.User

/**
  * Classe che rappresenta una cella
  *
  * @author Davide Borficchia
  * @author Eugenio Pierfederici
  * @param center punto nel quale verrà disegnata la cella
  * @param size   dimensione della cella
  */
case class ViewCell(center: Point, color: Color = defaultColor, size: Int = cellSize)

object CellImplicits {
  implicit def cellToViewCell(cell: Cell): ViewCell = ViewCell(cell.position, getColor(cell.owner), getSize(cell.energy))

  private def getColor(user: User): Color = {
    // Color based on the username hash value
    val color = new ColorHash().rgb(user.username)
    new Color(color.red, color.green, color.blue)
  }

  private def getSize(energy: Int): Int = energy
}
