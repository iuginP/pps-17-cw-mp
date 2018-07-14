package it.cwmp.client.view

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

/**
  * Trait that models a generic view.
  * Implements the main measures for a basic management of the view, delegating to the class
  * that extends it the choice of which components to use in the creation of the interface.
  */
trait FXView {

  protected def layout: String
  protected def title: String
  protected def stage: Stage
  protected def controller: FXController

  /**
    * Initialization of the view
    */
  private def initGUI(): Unit = {
    //creo un'istanza del file di layout
    val loader = new FXMLLoader(getClass.getResource(layout))
    loader.setController(controller)
    val pane: Pane = loader.load()

    //setto il titolo della finestra
    stage setTitle title
    stage setResizable false

    //stabilisco cosa fare alla chiusura della finestra
    stage.setOnCloseRequest( _ => {
      Platform.exit()
      System.exit(0)
    })
    //carico il layout nella scena e imposto la scena creata nello stage
    stage setScene new Scene(pane)
  }

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
  def hideGUI(): Unit = {
    // TODO: vedere se è un comportamento adatto a tutti o va astratto
    stage close()
  }
}
