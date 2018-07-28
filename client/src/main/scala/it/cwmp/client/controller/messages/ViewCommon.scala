package it.cwmp.client.controller.messages

/**
  * Collection of common View actor messages
  */
object ViewCommon {

  /**
    * Tells the receiver whi is the sender, so replies will be directed to it
    */
  case object Initialize

  /**
    * Shows the underlying graphical interface
    */
  case object Show

  /**
    * Hides the underlying graphical interface
    */
  case object Hide

}
