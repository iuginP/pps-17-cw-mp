package it.cwmp.client.view

import javafx.application.Platform
import javafx.scene.control.{Dialog, ProgressBar}
import javafx.scene.layout.BorderPane

/**
  * Classe per gestire il dialog di attessa
  *
  * @author Davide Borficchia
  */
trait FXLoadingDialog {

  this: FXController =>

  private var dialog = new Dialog[Boolean]()

  /**
    * Mostra un dialog customizzato
    * @param title titolo del dialog
    * @param message messaggio del dialog
    */
  def showLoadingDialog(title: String, message: String): Unit = {
    Platform.runLater(() => {
      dialog.setTitle(title)
      dialog.setHeaderText(message)
      val pane = new BorderPane()
      val infiniteProgress = new ProgressBar()
      pane.setCenter(infiniteProgress)
      dialog.getDialogPane.setContent(pane)
      dialog.setOnCloseRequest(dialog.getOnCloseRequest)
      dialog.show()
    })
  }

  /**
    * Metodo che chiude il dialog
    */
  def hideLoadingDialog(): Unit = {
    Platform.runLater(() => {
      dialog.setResult(true)
      dialog.hide()
    })
  }
}
