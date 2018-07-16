package it.cwmp.client.view

import java.util.Optional

import javafx.application.Platform
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, ButtonType}

trait FXAlerts {
  this: FXController =>

  def showInfo(title: String, message: String, onClose: Option[() => Unit] = None): Unit =
    genericAlert(AlertType.INFORMATION, title, s"$title", message, onClose)

  def showError(title: String, message: String, onClose: Option[() => Unit] = None): Unit =
    genericAlert(AlertType.ERROR, title, s"$title", message, onClose)

  private def genericAlert(alertType: AlertType, title: String, headerTitle: String, message: String, onClose: Option[() => Unit]): Unit = {
    Platform runLater(() => {
      val alert = new Alert(alertType, message, ButtonType.OK)
      alert setTitle title
      alert setHeaderText headerTitle
      val result: Optional[ButtonType] = alert.showAndWait
      if (result.get.eq(ButtonType.OK) && onClose.isDefined) {
        onClose.get()
      }
    })
  }
}
