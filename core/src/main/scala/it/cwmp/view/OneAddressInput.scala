package it.cwmp.view

import it.cwmp.view.AbstractAddressInput.{defaultPort, localIP}

/**
  * Class to request user a single pair IP and port
  *
  * @param viewTitle        the view title
  * @param message          the message to show the user
  * @param onResultReady    the action on result ready
  * @param onDialogCanceled the action on dialog canceled
  */
case class OneAddressInput(viewTitle: String, message: String)
                          (onResultReady: ((String, String)) => Unit, onDialogCanceled: Unit => Unit)
                          (defaultIP: String = localIP, defaultPort: String = defaultPort)
  extends AbstractAddressInput[(String, String)](viewTitle, message, onResultReady, onDialogCanceled)(defaultIP, defaultPort) {

  override protected def getResult: (String, String) = (addressIpField.getText, addressPortField.getText)
}
