package it.cwmp.view

import it.cwmp.view.AbstractAddressInput.{ADDRESS_LABEL, defaultPort, localIP}
import javafx.scene.control.{Label, TextField}
import javafx.scene.layout.GridPane

/**
  * A class to request user to insert two addresses
  *
  * @param viewTitle     the view title
  * @param message       the message to show the user
  * @param onResultReady the action on result ready
  */
case class TwoAddressesInput(viewTitle: String, message: String, onResultReady: (((String, String), (String, String))) => Unit)
                            (firstDefaultIP: String = localIP, firstDefaultPort: String = defaultPort,
                             secondDefaultIP: String = localIP, secondDefaultPort: String = defaultPort)
  extends AbstractAddressInput[((String, String), (String, String))](viewTitle, message, onResultReady)(firstDefaultIP, firstDefaultPort) {

  private var secondIpAddressValid = true
  private var secondPortValid = true

  protected var secondAddressIpField: TextField = _
  protected var secondAddressPortField: TextField = _

  override protected def wholeInputValid: Boolean = super.wholeInputValid && secondPortValid && secondIpAddressValid

  override protected def bodyGridFill(gridPane: GridPane): Unit = {
    super.bodyGridFill(gridPane)

    val secondIpAndPortFields = createIpAndPortFields()
    secondAddressIpField = secondIpAndPortFields._1
    secondAddressPortField = secondIpAndPortFields._2

    // attach listeners for changes that update button state
    secondAddressIpField.textProperty.addListener((_, _, newValue) => {
      this.secondIpAddressValid = ipAddressValid(newValue)
      updateButtonState()
    })
    secondAddressPortField.textProperty.addListener((_, _, newValue) => {
      this.secondPortValid = portValid(newValue)
      updateButtonState()
    })

    secondAddressIpField.setText(secondDefaultIP)
    secondAddressPortField.setText(secondDefaultPort)

    gridPane.add(new Label(ADDRESS_LABEL), 0, 1)
    gridPane.add(secondAddressIpField, 1, 1)
    gridPane.add(secondAddressPortField, 2, 1)
  }

  override protected def getResult: ((String, String), (String, String)) =
    ((addressIpField.getText, addressPortField.getText), (secondAddressIpField.getText, secondAddressPortField.getText))
}