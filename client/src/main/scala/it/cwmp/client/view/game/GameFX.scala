package it.cwmp.client.view.game

import it.cwmp.client.model.game.impl.CellWorld
import it.cwmp.client.view.game.model.CellView._
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.{Group, Scene}
import javafx.stage.Stage

/**
  * Questa classe permette di visualizzare una GUI statica che rappresenta uno stato del gioco
  *
  * @author Davide Borficchia
  */
case class GameFX() extends ObjectDrawer {

  var stage: Stage = _
  var root: Group = _
  var canvas: Canvas = _

  def start(title: String, size: Int): Unit = {
    new JFXPanel()
    Platform.runLater(() => {
      stage = new Stage
      root = new Group
      canvas = new Canvas(size, size)

      stage.setTitle(title)
      root.getChildren.add(canvas)
      stage.setScene(new Scene(root))

      //stabilisco cosa fare alla chiusura della finestra
      stage.setOnCloseRequest(_ => {
        Platform.exit()
        System.exit(0)
      })
      stage.show()
    })
  }

  def close(): Unit = {
    Platform.runLater(() => {
      stage.close()
    })
  }

  def updateWorld(world: CellWorld): Unit = {
    Platform.runLater(() => {
      implicit val graphicsContext: GraphicsContext = canvas.getGraphicsContext2D
      root.getChildren.clear()

      world.attacks.foreach(tentacle => root.getChildren.add(drawArch(tentacle, world.instant)))
      world.characters.foreach(cell => root.getChildren.add(drawCell(cell)))
      world.characters.foreach(cell => root.getChildren.add(drawCellEnergy(cell)))
    })
  }
}
