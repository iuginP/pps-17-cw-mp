package it.cwmp.client.view.game

import it.cwmp.client.model.game.impl.CellWorld
import it.cwmp.client.view.game.model.CellView._
import it.cwmp.client.view.game.model.TentacleView
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.{Group, Scene}
import javafx.stage.Stage

/**
  * This class shows the Game GUI
  *
  * @author Davide Borficchia
  */
case class GameFX() extends ObjectDrawer {

  private var stage: Stage = _
  private var root: Group = _
  private var canvas: Canvas = _

  /**
    * Initializes the GUI
    *
    * @param title the GUI title
    * @param size  the Window size
    */
  def start(title: String, size: Int): Unit = {
    new JFXPanel() // initializes JavaFX
    Platform.runLater(() => {
      stage = new Stage
      root = new Group
      canvas = new Canvas(size, size)

      stage.setTitle(title)
      root.getChildren.add(canvas)
      stage.setScene(new Scene(root))

      // what to do on window closed
      stage.setOnCloseRequest(_ => {
        Platform.exit()
        System.exit(0)
      })
      stage.show()
    })
  }

  /**
    * Closes the GUI
    */
  def close(): Unit = {
    Platform.runLater(() => {
      stage.close()
    })
  }

  /**
    * Updates the GUI with the newly provided world
    *
    * @param world the new world to draw
    */
  def updateWorld(world: CellWorld): Unit = {
    Platform.runLater(() => {
      implicit val graphicsContext: GraphicsContext = canvas.getGraphicsContext2D
      root.getChildren.clear()

      world.attacks.foreach(tentacle => root.getChildren.add(drawArch(TentacleView.tentacleToView(tentacle, world.instant))))
      world.characters.foreach(cell => root.getChildren.add(drawCell(cell)))
      world.characters.foreach(cell => root.getChildren.add(drawCellEnergy(cell)))
    })
  }
}
