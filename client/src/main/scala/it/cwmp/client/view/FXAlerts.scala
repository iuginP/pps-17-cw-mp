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

  def showLoading(headerText: String, message: String): Unit = createAndShowLoadingAlert(headerText, message)

  def hideLoading(): Unit = closeDialogAlert()

  //
  //  private def genericAlert(alertType: AlertType, title: String, headerTitle: String, message: String, onClose: Option[() => Unit]): Unit = {
  //    Platform runLater (() => {
  //      alert setTitle title
  //      alert setHeaderText headerTitle
  //      val result: Optional[ButtonType] = alert.showAndWait
  //      if (result.get.eq(ButtonType.OK) && onClose.isDefined) {
  //        onClose.get()
  //      }
  //    })
  //  }

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

  private var loadingAlert: Alert = _

  /**
    * Creates and shows a non blocking loading alert
    *
    * @param title   the title of the loading alert
    * @param message the message to show in the loading alert
    */
  private def createAndShowLoadingAlert(title: String, message: String): Unit =
    runOnUIThread(() => {
      loadingAlert = new Alert(AlertType.NONE)
      loadingAlert setTitle title
      loadingAlert setHeaderText message
      val pane = new BorderPane()
      pane.setCenter(new ProgressBar())
      loadingAlert.getDialogPane.setContent(pane)
      loadingAlert.show()
    })

  /**
    * Closes the loading alert
    */
  private def closeDialogAlert(): Unit =
    runOnUIThread(() => {
      loadingAlert.setResult(ButtonType.OK)
      loadingAlert.hide()
    })
}
