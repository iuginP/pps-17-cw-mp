package it.cwmp.view

import java.net.InetAddress

import it.cwmp.view.AbstractAddressInput._
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.control._
import javafx.scene.input.KeyCode
import javafx.scene.layout.GridPane

/**
  * A view to select an IP address and port
  *
  * @param viewTitle        the view title
  * @param message          the message to show the user
  * @param onResultReady    the action on result ready
  * @param onDialogCanceled the action to do on dialog canceled
  * @param defaultIP        the default IP for IP field
  * @param defaultPort      default port for Port field
  * @tparam Result the type of result
  * @author Enrico Siboni
  */
abstract class AbstractAddressInput[Result](viewTitle: String, message: String,
                                            onResultReady: Result => Unit, onDialogCanceled: Unit => Unit)
                                           (defaultIP: String = localIP, defaultPort: String = defaultPort) {
  new JFXPanel // initializes JavaFX

  private var dialog: Dialog[Result] = _
  private val dialogButtonType: ButtonType = ButtonType.FINISH
  private var bodyGrid: GridPane = _

  private var mouseOverOkButton = false
  private var enterKeyPressed = false
  private var ipAddressValid = true
  private var portValid = true

  protected var addressIpField: TextField = _
  protected var addressPortField: TextField = _

  /**
    * Validates IP Address
    *
    * @param ipAddress the ip address to validate
    * @return true if IP address is valid, false otherwise
    */
  protected def ipAddressValid(ipAddress: String): Boolean = ipAddress.nonEmpty && ipAddress.matches(IP_ADDRESS_PATTERN)

  /**
    * Validate port
    *
    * @param port the port to validate
    * @return true if port is valid, false otherwise
    */
  protected def portValid(port: String): Boolean = port.nonEmpty && port.forall(Character.isDigit)

  /**
    * @return true if whole input is valid, false otherwise
    */
  protected def wholeInputValid: Boolean = ipAddressValid && portValid

  /**
    * Updates the state of the button in view
    */
  protected def updateButtonState(): Unit =
    dialog.getDialogPane.lookupButton(dialogButtonType).setDisable(!wholeInputValid)

  /**
    * @return the result of user input
    */
  protected def getResult: Result

  /**
    * @return a pair of IP address and port TextFields
    */
  protected def createIpAndPortFields(): (TextField, TextField) = {
    val ipField = new TextField()
    ipField.setPromptText(IP_ADDRESS_TEXT)

    val portField = new TextField()
    portField.setPromptText(PORT_ADDRESS_TEXT)

    (ipField, portField)
  }

  /**
    * Fills the body grid of view
    *
    * @param gridPane the grid to fill
    */
  protected def bodyGridFill(gridPane: GridPane): Unit = {
    // Create a pair of fields IP and Address
    val ipAndPortFields = createIpAndPortFields()
    addressIpField = ipAndPortFields._1
    addressPortField = ipAndPortFields._2

    // attach listeners for changes that update button state
    addressIpField.textProperty.addListener((_, _, newValue) => {
      this.ipAddressValid = ipAddressValid(newValue)
      updateButtonState()
    })
    addressPortField.textProperty.addListener((_, _, newValue) => {
      this.portValid = portValid(newValue)
      updateButtonState()
    })

    addressIpField.setText(defaultIP)
    addressPortField.setText(defaultPort)

    // Request focus on the first field by default.
    addressIpField.requestFocus()

    gridPane.add(new Label(ADDRESS_LABEL), 0, 0)
    gridPane.add(addressIpField, 1, 0)
    gridPane.add(addressPortField, 2, 0)
  }

  // Initialization of View
  Platform.runLater(() => {
    Platform setImplicitExit false

    dialog = new Dialog[Result]
    dialog.setTitle(viewTitle)
    dialog.setHeaderText(message)
    dialog.getDialogPane.getButtonTypes.add(dialogButtonType)

    val buttonOk = dialog.getDialogPane.lookupButton(dialogButtonType)
    buttonOk.setOnMouseEntered(_ => mouseOverOkButton = true)
    buttonOk.setOnMouseExited(_ => mouseOverOkButton = false)

    dialog.getDialogPane.setOnKeyPressed(event => if (event.getCode == KeyCode.ENTER) enterKeyPressed = true)

    bodyGrid = new GridPane
    bodyGridFill(bodyGrid)

    dialog.getDialogPane.setContent(bodyGrid)

    dialog.setOnCloseRequest(_ => {
      if (wholeInputValid && (mouseOverOkButton || enterKeyPressed)) onResultReady(getResult)
      else onDialogCanceled(())
    })

    dialog.show()
  })
}

/**
  * Companion object
  */
object AbstractAddressInput {
  private val IP_ADDRESS_TEXT = "IP Address"
  private val PORT_ADDRESS_TEXT = "Address Port"

  private val IP_ADDRESS_PATTERN =
    "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
      "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
      "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
      "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"

  val ADDRESS_LABEL = "Address: "

  /**
    * @return the local network IP address
    */
  def localIP: String = InetAddress.getLocalHost.getHostAddress

  /**
    * @return the default port
    */
  def defaultPort: String = 0.toString
}
