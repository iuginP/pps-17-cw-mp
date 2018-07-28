package it.cwmp.client.controller

import akka.actor.Actor.Receive
import it.cwmp.client.controller.ViewVisibilityMessages.{Hide, Show}
import it.cwmp.client.view.{FXRunOnUIThread, FXViewController}

/**
  * A trait that give autonomous management to view visibility
  *
  * To use it you need to add "visibilityBehaviour" to your Actor receive
  *
  * @author Enrico Siboni
  */
trait ActorViewVisibilityManagement extends FXRunOnUIThread {

  /**
    * @return the visibility controller
    */
  protected def fxController: FXViewController

  /**
    * @return the behaviour that manages alert messages
    */
  protected def visibilityBehaviour: Receive = {
    case Show => onShowGUI()
    case Hide => onHideGUI()
  }

  /**
    * Called when GUI is shown
    */
  protected def onShowGUI(): Unit = runOnUIThread(() => fxController showGUI())

  /**
    * Called when GUI is hidden
    */
  protected def onHideGUI(): Unit = runOnUIThread(() => fxController hideGUI())
}


/**
  * A collection of view visibility messages
  */
object ViewVisibilityMessages {

  /**
    * Shows the underlying graphical interface
    */
  case object Show

  /**
    * Hides the underlying graphical interface
    */
  case object Hide

}