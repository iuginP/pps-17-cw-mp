package it.cwmp.client.view

import it.cwmp.client.controller.CreatePrivateRoomController
import it.cwmp.client.utils.{LayoutRes, StringRes}
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

class CreatePrivateRoomView extends View {

  var controller: CreatePrivateRoomController = _

  override def start(): Unit = {

    val mainStage = new Stage
    mainStage setTitle StringRes.CreatePrivateRoomTitle
    /*mainStage setHeight 400
    mainStage setWidth 600*/
    mainStage setResizable false

    val loader: FXMLLoader = new FXMLLoader(getClass.getResource(LayoutRes.createPrivateRoomLayout))
    val root: Pane = loader.load[Pane]
    controller = loader.getController[CreatePrivateRoomController]
    controller.stage = mainStage
    val scene: Scene = new Scene(root)

    mainStage.setOnCloseRequest((_) => {
      Platform.exit()
      System.exit(0)
    })
    mainStage setScene scene
    mainStage.show()

  }
}
