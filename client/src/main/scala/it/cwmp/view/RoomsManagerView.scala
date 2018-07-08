package it.cwmp.view

import it.cwmp.controller.RoomsManagerController
import it.cwmp.utils.{LayoutRes, StringRes}
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

class RoomsManagerView extends View{

    var controller: RoomsManagerController = _

    override def start(): Unit = {
      val mainStage = new Stage
      mainStage setTitle StringRes.RoomManagerTitle
      /*mainStage setHeight 400
      mainStage setWidth 600*/
      mainStage setResizable false

      val loader: FXMLLoader = new FXMLLoader(getClass.getResource(LayoutRes.roomManagerLayout))
      val root: Pane = loader.load[Pane]
      controller = loader.getController[RoomsManagerController]
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
