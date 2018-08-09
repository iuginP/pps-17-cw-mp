package it.cwmp.view

/**
  * A class to request user to insert two addresses
  *
  * @param viewTitle     the view title
  * @param message       the message to show the user
  * @param onResultReady the action on result ready
  */
case class TwoAddressesInput(viewTitle: String, message: String, onResultReady: (((String, String), (String, String))) => Unit)
                            (firstDefaultIP: String)
  extends AbstractAddressInput[((String, String), (String, String))](viewTitle, message, onResultReady)() {

  override protected def getResult: ((String, String), (String, String)) = ???
}

/**
  * Companion object
  */
object TwoAddressesInput {

}