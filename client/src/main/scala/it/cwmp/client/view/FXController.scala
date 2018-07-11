package it.cwmp.client.view

import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, ButtonType}

trait FXController {

  def showMessage(message: String): Unit =
    new Alert(AlertType.INFORMATION, message, ButtonType.OK) showAndWait

  def showError(message: String): Unit =
    new Alert(AlertType.ERROR, message, ButtonType.OK) showAndWait

  def resetFields(): Unit
}
