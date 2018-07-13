package it.cwmp.client.view.game

import it.cwmp.client.view.game.model._
import javafx.scene.canvas.GraphicsContext

/**
  *
  * @author Eugenio Pierfederici
  * @author Davide Borficchia
  */
trait ObjectDrawer {
  /**
    * Meodo utilizzato per disegnare una cella nella GUI
    *
    * @param cell oggetto che rappresenta la cella che verrà disegnata
    * @param graphicsContex è l'oggetto che disenga la cella
    */
  def drawCell(cell: Cell)(implicit graphicsContex: GraphicsContext): Unit = {
    graphicsContex.setFill(cell.color)
    graphicsContex.fillOval(cell.center.x-cell.size/2, cell.center.y-cell.size/2, cell.size, cell.size)
  }

  /**
    * Metodo utilizato per disegnare l'arco che unisce due celle
    *
    * @param firstCell è la cella dalla quale parte l'arco
    * @param secondCell è la cella nella quale arriva l'arco
    * @param graphicsContex è l'oggetto che disenga l'arco
    */
  def drawArch(firstCell: Cell, secondCell: Cell)(implicit graphicsContex: GraphicsContext): Unit = {
    graphicsContex.setStroke(firstCell.color)
    graphicsContex.strokeLine(firstCell.center.x, firstCell.center.y,
      secondCell.center.x, secondCell.center.y)
  }
}
