package it.cwmp.client.view

import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.layout.BorderPane

/**
  * A trait to manage dialogs in view
  *
  * @author Davide Borficchia
  */
trait FXDialogs {
  this: FXRunOnUIThread =>

  private val dialog = new Dialog[Unit]()

  /**
    * Show a loading Dialog with specified title and message
    *
    * @param title   the tile of the dialog
    * @param message the message in the dialog
    */
  def showLoadingDialog(title: String, message: String): Unit =
    runOnUIThread(() => {
      val pane = new BorderPane()
      pane.setCenter(new ProgressBar())
      showDialogWithContent(title, message, pane)
    })

  /**
    * Shows a dialog with specified parameters
    *
    * @param title         the title to set
    * @param message       the message to show
    * @param dialogContent the content on the dialog
    */
  def showDialogWithContent(title: String, message: String, dialogContent: Node): Unit =
    runOnUIThread(() => {
      dialog.setTitle(title)
      dialog.setHeaderText(message)
      dialog.getDialogPane.setContent(dialogContent)
      dialog.show()
    })

  /**
    * A method to close the open dialog
    */
  def hideDialog(): Unit =
    runOnUIThread(() => {
      dialog.setResult(())
      dialog.hide()
    })

}
