package it.cwmp.client.view

/**
  * A trait that gives generic methods that all JavaFX input controllers should have
  */
trait FXInputController {

  /**
    * Resets input fields
    */
  def resetFields(): Unit

  /**
    * Enables disabled view components
    */
  def enableViewComponents(): Unit
}
