package it.cwmp

import it.cwmp.view.OpeningView
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
