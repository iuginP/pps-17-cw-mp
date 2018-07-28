package it.cwmp.client.view

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.scene.control._
import javafx.scene.layout.{BorderPane, GridPane}

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

  def showTokenDialog(title: String, message: String): Unit = {
    Platform.runLater(() => {
      val tokenDialog = new Dialog[Boolean]
      tokenDialog.setTitle(title)

      val grid = new GridPane()
      grid.setHgap(10)
      grid.setVgap(10)

      val myToken = new TextField()
      val dialogBody = new Label("Token: ")
      val btnOk = new Button("OK")

      myToken.setEditable(false)
      myToken.setText(message)

      btnOk.setOnAction((e: ActionEvent) => {
        tokenDialog.setResult(true)
        tokenDialog.hide()
      })
      grid.add(dialogBody, 0, 0)
      grid.add(myToken, 0, 1)
      grid.add(btnOk, 1, 1)

      tokenDialog.getDialogPane.setContent(grid)
      tokenDialog.showAndWait()
    })
  }
}
