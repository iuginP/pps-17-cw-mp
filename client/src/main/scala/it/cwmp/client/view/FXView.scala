package it.cwmp.client.view

import it.cwmp.client.utils.StringRes
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

trait FXView {

  protected def layout(): String
  protected def title(): String
  protected def stage(): Stage
  protected def controller(): FXController

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

  //mostro la finestra
  def showGUI(): Unit = {
    initGUI()
    stage.show()
  }
}
