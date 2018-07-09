package it.cwmp.controller

import it.cwmp.view.{OpeningView, SignInView}
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.stage.Stage
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType

class OpeningController extends ViewController {

  var stage: Stage = _

  @FXML
  private def onClickSignIn(): Unit = {

    Platform.runLater(() => {
      val view: SignInView = new SignInView
      view.start()
    })
  }

  @FXML
  private def onClickSignUp(): Unit = {
    val alert = new Alert(AlertType.CONFIRMATION, "Prova prova?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
    alert.showAndWait

    if (alert.getResult eq ButtonType.YES) {

    }
  }
}
