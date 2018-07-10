package it.cwmp.client

import it.cwmp.client.view.CreatePrivateRoomView
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