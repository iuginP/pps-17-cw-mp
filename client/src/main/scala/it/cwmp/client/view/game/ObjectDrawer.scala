package it.cwmp.client.view.game

import java.time.Instant

import it.cwmp.client.model.game.impl.Tentacle
import it.cwmp.client.view.game.model._
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout._
import javafx.scene.paint.Color
import javafx.scene.shape.{Line, SVGPath}
import javafx.scene.text.Text

import scala.language.implicitConversions

/**
  *
  * @author Eugenio Pierfederici
  * @author Davide Borficchia
  */
trait ObjectDrawer {
  /**
    * Meodo utilizzato per disegnare una cella nella GUI
    *
    * @param cell            oggetto che rappresenta la cella che verrà disegnata
    */
  def drawCell(cell: CellView): Region = {
    val svg = new SVGPath
    //TODO cella hardcodata
    svg.setContent("M52.232,44.235c-1.039-0.285-2.039-0.297-2.969-0.112c-0.632,0.126-1.286-0.008-1.788-0.411   l-1.411-1.132c-0.766-0.614-0.992-1.665-0.585-2.559c0.377-0.829,0.69-1.693,0.932-2.587c0.402-1.487,2.008-2.394,3.444-1.838   c1.535,0.593,3.311,0.557,4.908-0.266c2.772-1.429,3.996-4.918,2.728-7.767c-1.364-3.066-4.967-4.413-8.002-3.009   c-0.266,0.123-0.563,0.312-0.868,0.535c-1.36,0.995-3.169,0.637-4.034-0.809c-0.546-0.913-1.215-1.741-1.882-2.571   c-0.883-1.098-1.037-2.618-0.387-3.878l1.479-2.871c0.55-1.068,1.57-1.871,2.765-1.988c0.603-0.059,1.226-0.22,1.865-0.512   c1.888-0.864,3.284-2.642,3.537-4.703c0.486-3.963-2.896-7.283-6.876-6.689c-2.527,0.377-4.589,2.411-4.996,4.933   c-0.197,1.221-0.025,2.386,0.423,3.404c0.459,1.045,0.499,2.226-0.024,3.241l-1.158,2.249c-0.805,1.563-2.612,2.394-4.29,1.87   c-0.981-0.306-2.718-0.523-3.92-0.644c-0.787-0.079-1.438-0.646-1.621-1.416L28.36,9.904c-0.141-0.594,0.036-1.207,0.435-1.669   c0.945-1.093,1.431-2.589,1.119-4.212c-0.371-1.933-1.91-3.514-3.838-3.913C22.849-0.557,20.009,1.89,20.009,5   c0,2.146,1.356,3.962,3.256,4.668c0.611,0.227,1.095,0.705,1.246,1.339l0.913,3.852c0.219,0.925-0.304,1.849-1.198,2.172   c-1.281,0.462-2.491,1.072-3.608,1.813c-0.802,0.531-1.895,0.519-2.642-0.086c-0.815-0.661-0.991-1.728-0.603-2.64   c0.628-1.474,0.829-3.173,0.429-4.95c-0.683-3.039-3.181-5.446-6.243-6.021c-5.63-1.057-10.471,3.79-9.402,9.422   c0.603,3.175,3.181,5.722,6.36,6.297c1.408,0.254,2.765,0.139,3.991-0.264c0.847-0.279,1.776,0.029,2.335,0.724l0.4,0.498   c0.574,0.714,0.636,1.706,0.167,2.493c-1.177,1.973-1.964,4.202-2.258,6.587c-0.122,0.992-0.904,1.771-1.9,1.86l-1.899,0.17   c-0.65,0.058-1.266-0.211-1.732-0.667c-0.721-0.705-1.688-1.181-2.83-1.258c-1.783-0.12-3.526,0.87-4.309,2.477   c-1.295,2.657,0.195,5.671,2.899,6.372c1.949,0.505,3.916-0.356,4.926-1.979c0.363-0.584,1.017-0.926,1.702-0.987l1.631-0.146   c0.987-0.088,1.888,0.529,2.192,1.472c0.669,2.076,1.728,3.977,3.089,5.618c0.487,0.587,0.459,1.443-0.051,2.009   c-0.425,0.472-1.085,0.609-1.69,0.416c-0.687-0.219-1.434-0.307-2.209-0.233c-2.498,0.237-4.582,2.233-4.913,4.721   c-0.497,3.738,2.765,6.871,6.537,6.15c1.964-0.376,3.596-1.867,4.172-3.783c0.28-0.93,0.303-1.829,0.139-2.661   c-0.092-0.468,0.051-0.95,0.37-1.305l0.785-0.871c0.472-0.524,1.243-0.673,1.862-0.336c0.489,0.266,0.993,0.508,1.51,0.726   c1.104,0.464,1.704,1.618,1.598,2.81c-0.051,0.575-0.019,1.174,0.11,1.787c0.528,2.504,2.683,4.44,5.228,4.703   c3.6,0.372,6.638-2.443,6.638-5.967c0-0.384-0.037-0.76-0.107-1.124c-0.23-1.199,0.257-2.415,1.325-3.006   c0.984-0.545,1.909-1.185,2.761-1.909c0.525-0.446,1.222-0.7,1.891-0.531c1.365,0.345,1.95,1.682,1.526,2.836   c-0.376,1.023-0.502,2.167-0.301,3.36c0.463,2.746,2.736,4.939,5.495,5.313c4.215,0.571,7.784-2.901,7.378-7.088   C56.721,47.208,54.792,44.938,52.232,44.235z")
    val svgShape = new Region
    svgShape.setShape(svg)
    // TODO: the cell size is not drawn according to size value!!!!
    svgShape.setBorder(new Border(new BorderStroke(Color.BLACK,
      BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)))
    svgShape.setMinSize(cell.radius, cell.radius)
    svgShape.setPrefSize(cell.radius, cell.radius) // TODO: la cellula ora fornisce il suo raggio, rivedere il dimensionamento (fare raggio * 2?)
    svgShape.setMaxSize(cell.radius, cell.radius)
    svgShape.setStyle("-fx-background-color: " + "#" + Integer.toHexString(cell.color.getRGB).substring(2))
    svgShape.setLayoutX(cell.center.x - cell.radius / 2)
    svgShape.setLayoutY(cell.center.y - cell.radius / 2)
    svgShape
  }

  /**
    * Metodo per disegnare l'energia di una cella
    *
    * @param cell cella della quale disegnare l'energia
    * @return il testo da aggiungere alla scena
    */
  def drawCellEnergy(cell: CellView): Text = {
    val energyText = new Text(cell.center.x, cell.center.y, cell.energy.toInt.toString)
    energyText.setFont(CellView.ENERGY_FONT)
    energyText.setFill(Color.BLACK) // TODO: move to a coloring strategy inside CellView
    energyText.setX(cell.center.x - (energyText.getLayoutBounds.getWidth / 2))
    energyText.setY(cell.center.y + (energyText.getLayoutBounds.getHeight / 2))
    energyText
  }

  /**
    * Metodo utilizato per disegnare il tentacolo che unisce due celle
    *
    * @param tentacle      il tentacolo da disegnare
    * @param actualInstant l'istante di gioco
    * @return la linea da visualizzare nella GUI
    */
  def drawArch(tentacle: Tentacle, actualInstant: Instant): Line = {
    val line = new Line()
    line.setStroke(TentacleView.coloringStrategy(tentacle))
    line.setStrokeWidth(TentacleView.thicknessStrategy(tentacle))

    val attackerPosition = tentacle.from.position
    val tentacleReachedPoint = TentacleView.reachedPoint(tentacle, actualInstant)
    line.setStartX(attackerPosition.x)
    line.setStartY(attackerPosition.y)
    line.setEndX(tentacleReachedPoint.x)
    line.setEndY(tentacleReachedPoint.y)
    line
  }

  /**
    * Funzione per convertire i colori javaFX nel formato di colori utilizzato da java.awt
    *
    * @param fxColor Colore che si vuole convertire
    * @return il colore awt
    */
  private implicit def fxColorToAwtColor(fxColor: javafx.scene.paint.Color): java.awt.Color = {
    new java.awt.Color(fxColor.getRed.toFloat, fxColor.getGreen.toFloat, fxColor.getBlue.toFloat, fxColor.getOpacity.toFloat)
  }
}
