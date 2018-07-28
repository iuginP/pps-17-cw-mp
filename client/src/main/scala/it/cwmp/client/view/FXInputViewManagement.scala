package it.cwmp.client.view

/**
  * A trait that gives generic methods that all JavaFX input controllers should have
  */
trait FXInputViewManagement {

  /**
    * Resets input fields
    */
  def resetFields(): Unit

  /**
    * Disables View components
    */
  def disableViewComponents(): Unit

  /**
    * Enables disabled view components
    */
  def enableViewComponents(): Unit
}
