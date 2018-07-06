package it.cwmp

import it.cwmp.controller.CreatePrivateRoomController
import it.cwmp.view.CreatePrivateRoomView
import javafx.application.Platform
import javafx.embed.swing.JFXPanel

object CreatePrivateRoomMain extends App {
  new JFXPanel
  Platform setImplicitExit false
  Platform runLater(() => {
    val createPrivateRoomView = new CreatePrivateRoomView
    createPrivateRoomView.start()
  })
}
