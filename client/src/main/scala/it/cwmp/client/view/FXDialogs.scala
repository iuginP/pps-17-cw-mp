package it.cwmp.client.view

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.scene.control._
import javafx.scene.layout.{BorderPane, GridPane}

/**
  * Classe per gestire il dialog di attessa
  *
  * @author Davide Borficchia
  */
trait FXDialogs {

  //this: FXController =>

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
      dialog.show()
    })
  }

  /**
    * Metodo che chiude il dialog di loading
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
      grid.add(dialogBody,0,0)
      grid.add(myToken,0,1)
      grid.add(btnOk, 1,1)

      tokenDialog.getDialogPane.setContent(grid)
      tokenDialog.showAndWait()
    })
  }
}
