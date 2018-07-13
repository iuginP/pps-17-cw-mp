package it.cwmp.client.view

import javafx.application.Platform
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, ButtonType}

trait FXAlerts {
  this: FXController =>

  def showInfo(title: String, message: String): Unit = genericAlert(AlertType.INFORMATION, title, s"$title:", message)

  def showError(title: String, message: String): Unit = genericAlert(AlertType.ERROR, title, s"$title!", message)

  private def genericAlert(alertType: AlertType, title: String, headerTitle: String, message: String): Unit = {
    Platform runLater(() => {
      val alert = new Alert(alertType, message, ButtonType.OK)
      alert setTitle title
      alert setHeaderText headerTitle
      alert showAndWait
    })
  }
}
