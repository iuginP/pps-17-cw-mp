package it.cwmp.view

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

class OpeningView extends View {

  /**
    * Main method that starts this view.
    **/
  override def start(): Unit = {
    val mainStage = new Stage
    mainStage setTitle "asdasd"
    mainStage setHeight 125
    mainStage setWidth 383
    mainStage setResizable false
    val loader: FXMLLoader = new FXMLLoader(getClass.getResource("/openingTemplate.fxml"))
    val root: Pane = loader.load[Pane]
    //controller = loader.getController[LoaderController]
    //controller.stage = mainStage
    val scene: Scene = new Scene(root)
    mainStage.setOnCloseRequest((_) => {
      Platform.exit()
      System.exit(0)
    })
    mainStage setScene scene
    mainStage.show()
  }
}
