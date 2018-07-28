package it.cwmp.client.view.authentication

import it.cwmp.client.utils.{LayoutRes, StringRes}
import it.cwmp.client.view._
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control._

/**
  * Class that models the controller that manages the various authentication processes.
  *
  * @param strategy strategy to be applied to resolve authentication requests.
  */
class AuthenticationFXController(strategy: AuthenticationStrategy) extends FXInputViewController
  with FXView with FXInputChecks with FXAlerts with FXRunOnUIThread {

  protected val layout: String = LayoutRes.authenticationLayout
  protected val title: String = StringRes.appName
  protected val controller: FXInputViewController = this

  @FXML
  private var tpMain: TabPane = _
  @FXML
  private var tfSignInUsername: TextField = _
  @FXML
  private var pfSignInPassword: PasswordField = _
  @FXML
  private var tfSignUpUsername: TextField = _
  @FXML
  private var pfSignUpPassword: PasswordField = _
  @FXML
  private var pfSignUpConfirmPassword: PasswordField = _
  @FXML
  private var btnSignIn: Button = _
  @FXML
  private var btnSignInReset: Button = _
  @FXML
  private var btnSignUp: Button = _
  @FXML
  private var btnSignUpReset: Button = _


  @FXML
  private def onClickSignIn(): Unit = {
    Platform.runLater(() => {
      for (
        username <- getTextFieldValue(tfSignInUsername, "È necessario inserire lo username");
        password <- getTextFieldValue(pfSignInPassword, "È necessario inserire la password")
      ) yield {
        btnSignIn.setDisable(true)
        btnSignInReset.setDisable(true)
        showLoading("Attendere", "login in corso")
        strategy.performLogIn(username, password)
      }
    })
  }

  @FXML
  private def onClickSignUp(): Unit = {
    Platform.runLater(() => {
      for (
        username <- getTextFieldValue(tfSignUpUsername, "È necessario inserire lo username");
        password <- getTextFieldValue(pfSignUpPassword, "È necessario inserire la password");
        confirmPassword <- getTextFieldValue(pfSignUpConfirmPassword, "È necessario inserire nuovamente la password")
      ) yield {
        // TODO: rivedere questa logica per farne una più specifica
        showLoading("Attendere", "Registrazione in corso")
        if (strategy.performPasswordCheck(password, confirmPassword)) {
          strategy.performSignUp(username, password)
          btnSignUp.setDisable(true)
          btnSignUpReset.setDisable(true)
        } else {
          showError("Warning", "Non-compliant passwords!")
          hideLoading()
          btnSignUp.setDisable(false)
          btnSignUpReset.setDisable(false)
        }
      }
    })
  }

  @FXML
  private def onClickSignInReset(): Unit = {
    Platform.runLater(() => {
      resetFields()
    })
  }

  @FXML
  private def onClickSignUpReset(): Unit = {
    Platform.runLater(() => {
      resetFields()
    })
  }

  override def resetFields(): Unit = {
    if (tpMain.getSelectionModel.getSelectedIndex == 0) {
      tfSignInUsername setText ""
      pfSignInPassword setText ""
    } else {
      tfSignUpUsername setText ""
      pfSignUpPassword setText ""
      pfSignUpConfirmPassword setText ""
    }
  }

  override def enableViewComponents(): Unit = {
    btnSignInReset.setDisable(false)
    btnSignIn.setDisable(false)
    btnSignUpReset.setDisable(false)
    btnSignUp.setDisable(false)
  }
}

/**
  * [[AuthenticationFXController]] companion object
  *
  * @author Elia Di Pasquale
  */
object AuthenticationFXController {
  def apply(strategy: AuthenticationStrategy): AuthenticationFXController = {
    require(strategy != null, "The authentication strategy cannot be null")
    new AuthenticationFXController(strategy)
  }
}
