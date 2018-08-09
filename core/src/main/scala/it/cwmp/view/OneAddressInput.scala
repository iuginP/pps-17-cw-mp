package it.cwmp.view

import it.cwmp.view.AbstractAddressInput.{defaultPort, localIP}

/**
  * Class to request user a single pair IP and port
  *
  * @param viewTitle     the view title
  * @param message       the message to show the user
  * @param onResultReady the action on result ready
  */
class OneAddressInput(viewTitle: String, message: String, onResultReady: ((String, String)) => Unit)
                     (defaultIP: String = localIP, defaultPort: String = defaultPort)
  extends AbstractAddressInput[(String, String)](viewTitle, message, onResultReady)(defaultIP, defaultPort) {

  override protected def getResult: (String, String) = (addressIpField.getText, addressPortField.getText)
}

/**
  * Companion object
  */
object OneAddressInput {

  def apply(viewTitle: String, message: String, onResultReady: ((String, String)) => Unit)
           (defaultIP: String = localIP, defaultPort: String = defaultPort): OneAddressInput =
    new OneAddressInput(viewTitle, message, onResultReady)(defaultIP, defaultPort)
}
