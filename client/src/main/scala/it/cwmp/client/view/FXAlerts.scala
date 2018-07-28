package it.cwmp.client.view

import javafx.scene.Node
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, ButtonType, ProgressBar}
import javafx.scene.layout.BorderPane

/**
  * A trait that makes possible to show Alerts
  *
  * @author Eugenio Pierfederici
  * @author contributor Davide Borficchia
  * @author contributor Enrico Siboni
  */
trait FXAlerts extends FXRunOnUIThread {

  /**
    * A method to show an info message
    *
    * @param headerText the text to show as header
    * @param message    the specific message
    */
  def showInfo(headerText: String, message: String): Unit =
    showAlertWithButtons(AlertType.INFORMATION, headerText, message, ButtonType.OK)

  /**
    * An info alert with custom content
    *
    * @param headerText   the header text to show
    * @param message      the message to show
    * @param alertContent the custom content to show
    */
  def showInfoWithContent(headerText: String, message: String, alertContent: Node): Unit =
    showAlertWithButtons(AlertType.INFORMATION, headerText, message, ButtonType.OK, Some(alertContent))

  /**
    * A method to show an error message
    *
    * @param headerText the text to show as header
    * @param message    the specific message
    */
  def showError(headerText: String, message: String): Unit =
    showAlertWithButtons(AlertType.ERROR, headerText, message, ButtonType.OK)

  /**
    * Shows a loading dialog that either can be closed or not
    *
    * @param headerText               the header text to show
    * @param message                  the message to show
    * @param canBeClosed              whether the dialog ca be closed or not
    * @param onPrematureClosureAction which action has to be executed if the loading is closed prematurely
    */
  def showLoading(headerText: String, message: String,
                  canBeClosed: Boolean = false,
                  onPrematureClosureAction: () => Unit = () => ()): Unit = runOnUIThread(() => {
    LoadingManagement
      .createLoadingAlert(headerText, message, canBeClosed, onPrematureClosureAction)
      .show()
  })

  /**
    * Hides the loading dialog
    */
  def hideLoading(): Unit = LoadingManagement.closeLoadingAlert()

  /**
    * Shows an alert with specified parameters
    *
    * @param alertType     the alert type
    * @param headerText    the title to set
    * @param message       the message to show
    * @param buttonType    the buttonType
    * @param dialogContent the content on the dialog
    */
  private def showAlertWithButtons(alertType: AlertType, headerText: String,
                                   message: String, buttonType: ButtonType,
                                   dialogContent: Option[Node] = None): Unit =
    runOnUIThread(() => {
      val alert = new Alert(alertType, message, buttonType)
      alert setTitle alertType.toString
      alert setHeaderText headerText
      dialogContent foreach (alert.getDialogPane.setContent(_))
      alert.showAndWait
    })


  /**
    * An object that wraps Loading alert management
    */
  private object LoadingManagement {
    var loadingAlert: Alert = _

    /**
      * Creates and shows a non blocking loading alert
      *
      * @param title   the title of the loading alert
      * @param message the message to show in the loading alert
      */
    def createLoadingAlert(title: String, message: String,
                           canBeClosed: Boolean, onPrematureCloseAction: () => Unit): Alert = {
      loadingAlert = new Alert(AlertType.NONE)
      loadingAlert setTitle title
      loadingAlert setHeaderText message
      val pane = new BorderPane()
      pane.setCenter(new ProgressBar())
      loadingAlert.getDialogPane.setContent(pane)

      if (canBeClosed) {
        loadingAlert.getButtonTypes.add(ButtonType.CANCEL)
        loadingAlert.setOnCloseRequest(_ => onPrematureCloseAction())
      }

      loadingAlert
    }

    /**
      * Closes the loading alert
      */
    def closeLoadingAlert(): Unit =
      runOnUIThread(() => {
        //noinspection ScalaStyle
        loadingAlert.setOnCloseRequest(null)
        loadingAlert.setResult(ButtonType.OK) // setting a result of right type, seems the only way to have dialog to close
      })
  }

}
