package it.cwmp.client.view

import javafx.application.Platform
import javafx.scene.control._
import javafx.scene.layout.BorderPane

/**
  * A trait to manage loading dialogs in view
  *
  * @author Davide Borficchia
  */
trait FXLoadingDialogs {

  private val dialog = new Dialog[Boolean]()

  /**
    * Show a loading Dialog with specified title and message
    *
    * @param title   the tile of the dialog
    * @param message the message in the dialog
    */
  def showLoadingDialog(title: String, message: String): Unit = {
    Platform.runLater(() => {
      dialog.setTitle(title)
      dialog.setHeaderText(message)
      val pane = new BorderPane()
      val infiniteProgress = new ProgressBar()
      pane.setCenter(infiniteProgress)
      dialog.getDialogPane.setContent(pane)
      dialog.show()
    })
  }

  /**
    * A method to close the loading dialog
    */
  def hideLoadingDialog(): Unit = {
    Platform.runLater(() => {
      dialog.setResult(true)
      dialog.hide()
    })
  }
}
