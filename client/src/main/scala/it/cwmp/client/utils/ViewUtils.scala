package it.cwmp.client.utils

import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, ButtonType}

object ViewUtils {

  def showDialog(alertType: AlertType = AlertType.WARNING, message: String,
                 firstButton: ButtonType = ButtonType.OK, secondButton: ButtonType = ButtonType.CANCEL,
                 onFirst: => Unit = {}, onSecond: => Unit = {}): Unit = {
    val alert = new Alert(alertType, message, firstButton, secondButton)
    alert.showAndWait
    alert.getResult match {
      case `firstButton` => onFirst
      case `secondButton` => onSecond
      case _ =>
    }
  }
}
