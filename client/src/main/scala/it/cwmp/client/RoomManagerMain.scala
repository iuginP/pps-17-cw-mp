package it.cwmp.client

import it.cwmp.client.view.RoomsManagerView
import javafx.application.Platform
import javafx.embed.swing.JFXPanel

object RoomManagerMain extends App {

  new JFXPanel
  Platform setImplicitExit false
  Platform runLater(() => {
    val roomsManagerView = new RoomsManagerView
    roomsManagerView.start()
  })

}