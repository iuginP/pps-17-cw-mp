package it.cwmp.client.controller

import it.cwmp.client.utils.ViewUtils
import it.cwmp.client.view.{OpeningView, SignInView, SignUpView}
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
    val view: SignUpView = new SignUpView
    view.start()
  }
}
