package it.cwmp.client.view

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

/**
  * Trait that models a generic view.
  * Implements the main measures for a basic management of the view, delegating to the class
  * that extends it the choice of which components to use in the creation of the interface.
  */
trait FXViewController {

  protected val stage: Stage = new Stage

  /**
    * Method called to show the view
    */
  def showGUI(): Unit = {
    initGUI()
    stage show()
  }

  /**
    * Method called to hide the view
    */
  def hideGUI(): Unit = stage close()

  /**
    * @return the path to layout resource
    */
  protected def layout: String

  /**
    * @return the view title string
    */
  protected def title: String

  /**
    * @return the FXViewController for this view
    */
  protected def controller: FXViewController

  /**
    * Tells which action to perform on window close
    *
    * @return the default close action (exits JVM)
    */
  protected def onCloseAction(): Unit = {
    Platform.exit()
    System.exit(0)
  }

  /**
    * Initialization of the view
    */
  protected def initGUI(): Unit = {
    new JFXPanel // initializes JavaFX

    // creates an instance of layout
    val loader = new FXMLLoader(getClass.getResource(layout))
    loader.setController(controller)
    val pane: Pane = loader.load()

    stage setTitle title
    stage setOnCloseRequest (_ => onCloseAction())
    stage setScene new Scene(pane)
    stage setResizable false
    Platform setImplicitExit false
  }
}
