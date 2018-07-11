package it.cwmp.client.controller

import it.cwmp.exceptions.HTTPException
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.{Label, PasswordField, TextField}
import javafx.scene.layout.VBox
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.StageStyle

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class SignInController extends AuthenticationController {

  @FXML
  private var tfUsername: TextField = _

  @FXML
  private var pfPassword: PasswordField = _


  @FXML
  private def onClickSignIn(): Unit = {
    val username = tfUsername.getText
    val password = pfPassword.getText

    // TODO: various controls

    auth.login(username, password).onComplete {
      case Success(value) => println(s"Sign in completed! Token: $value")
      case Failure(HTTPException(statusCode, errorMessage)) => statusCode match {
        case 400 => println(s"Error! Code: 400") // TODO: handle error
        case 401 => println(s"Error! Code: 401") // TODO: handle error
        case _ => // TODO: handle error default
      }
    }
  }

}
