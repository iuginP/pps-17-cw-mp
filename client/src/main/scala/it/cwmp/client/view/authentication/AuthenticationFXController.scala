package it.cwmp.client.view.authentication

import it.cwmp.client.utils.{LayoutRes, StringRes}
import it.cwmp.client.view._
import it.cwmp.client.view.authentication.AuthenticationFXController._
import javafx.fxml.FXML
import javafx.scene.control._

/**
  * Class that models the controller that manages the various authentication processes.
  *
  * @param strategy strategy to be applied to resolve authentication requests.
  * @author contributor Enrico Siboni
  */
class AuthenticationFXController(strategy: AuthenticationStrategy) extends FXViewController with FXInputViewController with FXInputChecks {

  protected val layout: String = LayoutRes.authenticationLayout
  protected val title: String = StringRes.appName
  protected val controller: FXViewController = this

  @FXML private var tpMain: TabPane = _
  @FXML private var tfSignInUsername: TextField = _
  @FXML private var pfSignInPassword: PasswordField = _
  @FXML private var tfSignUpUsername: TextField = _
  @FXML private var pfSignUpPassword: PasswordField = _
  @FXML private var pfSignUpConfirmPassword: PasswordField = _
  @FXML private var btnSignIn: Button = _
  @FXML private var btnSignInReset: Button = _
  @FXML private var btnSignUp: Button = _
  @FXML private var btnSignUpReset: Button = _

  override protected def initGUI(): Unit = {
    super.initGUI()
    // adds a listener to reset fields on tab change
    tpMain.getSelectionModel.selectedItemProperty.addListener((_, _, _) => resetFields())
    btnSignIn.setDefaultButton(true)
  }

  override def showGUI(): Unit = {
    super.showGUI()
    tfSignInUsername.requestFocus()
  }

  override def resetFields(): Unit = {
    tfSignInUsername setText ""
    pfSignInPassword setText ""
    tfSignUpUsername setText ""
    pfSignUpPassword setText ""
    pfSignUpConfirmPassword setText ""
  }

  override def disableViewComponents(): Unit = {
    btnSignInReset.setDisable(true)
    btnSignIn.setDisable(true)
    btnSignUpReset.setDisable(true)
    btnSignUp.setDisable(true)
  }

  override def enableViewComponents(): Unit = {
    btnSignInReset.setDisable(false)
    btnSignIn.setDisable(false)
    btnSignUpReset.setDisable(false)
    btnSignUp.setDisable(false)
  }

  @FXML private def onClickSignIn(): Unit =
    runOnUIThread(() => {
      for (
        username <- getTextFieldValue(tfSignInUsername, USERNAME_EMPTY_ERROR);
        password <- getTextFieldValue(pfSignInPassword, PASSWORD_EMPTY_ERROR)
      ) yield
        strategy.performLogIn(username, password)
    })


  @FXML private def onClickSignUp(): Unit =
    runOnUIThread(() => {
      for (
        username <- getTextFieldValue(tfSignUpUsername, USERNAME_EMPTY_ERROR);
        password <- getTextFieldValue(pfSignUpPassword, PASSWORD_EMPTY_ERROR);
        confirmPassword <- getTextFieldValue(pfSignUpConfirmPassword, REPEAT_PASSWORD_EMPTY_ERROR)
      ) yield
        if (strategy.performPasswordCheck(password, confirmPassword)) {
          strategy.performSignUp(username, password)
        } else {
          showError(ATTENTION_MESSAGE, GIVEN_PASSWORD_DOES_NOT_MATCH)
        }
    })

  @FXML private def onClickSignInReset(): Unit = runOnUIThread { () => resetFields() }

  @FXML private def onClickSignUpReset(): Unit = runOnUIThread { () => resetFields() }
}

/**
  * Companion object
  *
  * @author Elia Di Pasquale
  */
object AuthenticationFXController {
  def apply(strategy: AuthenticationStrategy): AuthenticationFXController = {
    require(strategy != null, "The authentication strategy cannot be null")
    new AuthenticationFXController(strategy)
  }

  private val USERNAME_EMPTY_ERROR = "È necessario inserire lo username"
  private val PASSWORD_EMPTY_ERROR = "È necessario inserire la password"
  private val REPEAT_PASSWORD_EMPTY_ERROR = "È necessario inserire nuovamente la password"
  private val GIVEN_PASSWORD_DOES_NOT_MATCH = "Le password inserite non sono uguali!"

  private val ATTENTION_MESSAGE = "Attenzione"
}
