package it.cwmp.client.controller

import javafx.fxml.FXML
import javafx.scene.control.{PasswordField, TextField}
import javax.xml.ws.http.HTTPException

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class SignUpController extends AuthenticationController {

  @FXML
  private var tfUsername: TextField = _

  @FXML
  private var pfPassword: PasswordField = _

  @FXML
  private var pfPasswordConfirm: PasswordField = _


  @FXML
  private def onClickSignUp(): Unit = {
    val username = tfUsername.getText
    val password = pfPassword.getText
    val passwordConfirm = pfPasswordConfirm.getText

    // TODO: various controls

    auth.signUp(username, password).onComplete {
      case Success(value) => println(s"Sign up completed! Token: $value")
      case Failure(e: HTTPException) => e.getStatusCode match {
        case 400 => println(s"Error! Code: 400") // TODO: handle error
        case _ => // TODO: handle error default
      }
    }
  }
}
