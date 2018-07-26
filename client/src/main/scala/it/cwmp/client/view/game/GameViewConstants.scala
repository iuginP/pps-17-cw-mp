package it.cwmp.client.view.game

import javafx.scene.paint.Color
import javafx.scene.text.Font

/**
  * An object where to put constants about the game visual
  *
  * @author Enrico Siboni
  */
object GameViewConstants {
  /**
    * The constant value indicating the transparency of instant text in the GUI
    */
  val TRANSPARENCY_INSTANT: Double = 0.5


  /**
    * The constant value indicating the rgb range max value
    */
  val RGB_RANGE = 255.0

  /**
    * The game default font size
    */
  val GAME_DEFAULT_FONT_SIZE = 20

  /**
    * The game default font color
    */
  val GAME_DEFAULT_FONT_COLOR: Color = Color.BLACK

  /**
    * The game time default font
    */
  val GAME_TIME_TEXT_FONT: Font = Font.font("Verdana", GAME_DEFAULT_FONT_SIZE)

  /**
    * The game time default font color
    */
  val GAME_TIME_TEXT_COLOR: Color = Color.rgb(GAME_DEFAULT_FONT_COLOR.getRed.toInt, GameViewConstants.GAME_DEFAULT_FONT_COLOR.getGreen.toInt,
    GameViewConstants.GAME_DEFAULT_FONT_COLOR.getBlue.toInt, GameViewConstants.TRANSPARENCY_INSTANT)
}
