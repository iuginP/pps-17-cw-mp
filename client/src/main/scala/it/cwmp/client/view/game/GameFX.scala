package it.cwmp.client.view.game

import akka.actor.ActorRef
import it.cwmp.client.model.game.impl.{CellWorld, Point}
import it.cwmp.client.view.game.model.CellView._
import it.cwmp.client.view.game.model.TentacleView
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.input.MouseEvent
import javafx.scene.{Group, Scene}
import javafx.stage.Stage

import scala.language.implicitConversions

/**
  * This class shows the Game GUI
  *
  * @author Davide Borficchia
  * @author contributor Enrico Siboni
  */
case class GameFX() extends CellWorldObjectDrawer {

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

      UserEventHandler.initializeEventHandlers(root)

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

      world.attacks.foreach(tentacle => root.getChildren.add(drawTentacle(TentacleView.tentacleToView(tentacle, world.instant))))
      world.characters.foreach(cell => root.getChildren.add(drawCell(cell)))
      world.characters.foreach(cell => root.getChildren.add(drawCellEnergy(cell)))
      root.getChildren.add(drawInstant(world.instant))
    })
  }

  /**
    * An object that wraps logic behind user events un GUI
    *
    * @author Enrico Siboni
    */
  private object UserEventHandler {

    private var startDragPoint: Option[Point] = None

    /**
      * A method to initialize event handlers for GUI user actions
      *
      * @param viewGroup the viewGroup on which to listen for events
      */
    def initializeEventHandlers(viewGroup: Group): Unit = {

      // start of user dragging
      viewGroup.addEventHandler(MouseEvent.DRAG_DETECTED,
        (event: MouseEvent) => startDragPoint = Some(event))

      // stop of user dragging
      viewGroup.addEventHandler(MouseEvent.MOUSE_RELEASED, (event: MouseEvent) =>
        if (startDragPoint.isDefined) {
          val stopDragPoint: Point = event
          // TODO: send a message to GameViewActor
        })

      // user click event
      viewGroup.addEventHandler(MouseEvent.MOUSE_CLICKED, (event: MouseEvent) =>
        // if the user was dragging this event is launched after MOUSE_RELEASED
        if (startDragPoint.isDefined) {
          startDragPoint = None // reset user dragging state
        } else {
          val clickedPoint: Point = event
          // TODO: send a message to GameViewActor
        }
      )
    }

    /**
      * A method to send the AddAttack message to provided actor
      *
      * @param start the start point of the attack
      * @param stop  the stop point of the attack
      * @param actor the actor responsible of this management
      */
    private def sendAddAttackEvent(start: Point, stop: Point)(implicit actor: ActorRef): Unit =
      actor ! GameViewActor.AddAttack(start, stop)

    /**
      * A method to send the RemoveAttack messsage to provided actor
      *
      * @param onAttackPoint the point on the Attack View to remove
      * @param actor         the actor responsible of this management
      */
    private def sendRemoveAttackEvent(onAttackPoint: Point)(implicit actor: ActorRef): Unit =
      actor ! GameViewActor.RemoveAttack(onAttackPoint)

    /**
      * An implicit conversion from mouse event to the point where event was generated
      *
      * @param event the event to convert
      * @return the Point where event was generated
      */
    implicit def eventToPoint(event: MouseEvent): Point =
      Point(event.getX.toInt, event.getY.toInt)
  }

}
