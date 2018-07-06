package it.cwmp.client.controller

import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.authentication.AuthenticationService
import javafx.fxml.FXML
import javafx.scene.control.{PasswordField, TextField}

class SignUpController extends ViewController {

  private val auth = AuthenticationService(
    WebClient.create(Vertx.vertx(),
      WebClientOptions()
        .setDefaultHost("localhost")
        .setDefaultPort(8666)
        .setKeepAlive(false))
  )

  @FXML
  private var tfUsername: TextField = _

  @FXML
  private var pfPassword: PasswordField = _

  @FXML
  private var pfPasswordConfirm: PasswordField = _


  @FXML
  private def onClickSignUp(): Unit = {
    val username = tfUsername getText
    val password = pfPassword getText
    val passwordConfirm = pfPasswordConfirm getText
    // TODO: various controls

  }

}
