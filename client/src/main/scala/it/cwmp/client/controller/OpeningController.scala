package it.cwmp.client.controller

import it.cwmp.client.view.{SignInView, SignUpView}
import javafx.application.Platform
import javafx.fxml.FXML

class OpeningController extends ViewController {

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
