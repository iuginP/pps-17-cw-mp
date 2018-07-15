package it.cwmp.client.view.game

import java.awt.Color

import it.cwmp.client.model.game.{Point, World}
import it.cwmp.client.view.game.model._
import javafx.application.{Application, Platform}
import javafx.embed.swing.JFXPanel
import javafx.scene.canvas.Canvas
import javafx.scene.{Group, Scene}
import javafx.stage.Stage


object GameFX {
  def apply(): GameFX = new GameFX()
}

/**
  * Questa classe permette di visualizzare una GUI statica che rappresenta uno stato del gioco
  *
  * @author Davide Borficchia
  */
class GameFX extends ObjectDrawer {

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
      stage.setOnCloseRequest( _ => {
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

  def updateWorld(world: World): Unit = {
    Platform.runLater(() => {
      implicit val graphicsContext = canvas.getGraphicsContext2D
      import it.cwmp.client.view.game.model.CellImplicits._

      world.tentacles.foreach(tentacle => drawArch(tentacle.startCell, tentacle.arriveCell))
      world.cells.foreach(cell => root.getChildren.add(drawCell(cell)))
    })
  }
}
