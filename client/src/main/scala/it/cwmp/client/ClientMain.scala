package it.cwmp.client

import it.cwmp.client.view.OpeningView
import javafx.application.Platform
import javafx.embed.swing.JFXPanel

object ClientMain extends App {
  new JFXPanel
  Platform setImplicitExit false
  Platform runLater(() => {
    val openingView = new OpeningView
    openingView.start()
  })
}
