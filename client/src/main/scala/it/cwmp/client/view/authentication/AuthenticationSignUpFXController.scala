package it.cwmp.client.view.authentication

import it.cwmp.client.utils.{LayoutRes, StringRes}
import it.cwmp.client.view.{FXAlerts, FXChecks, FXController, FXView}
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.stage.Stage

trait SignUpFXStrategy {
  def onSignUp(username: String, password: String): Unit
}

object SignUpFXController {
  def apply(strategy: SignUpFXStrategy): SignUpFXController = {
    require(strategy != null)
    new SignUpFXController(strategy)
  }
}

class SignUpFXController(strategy: SignUpFXStrategy) extends FXController with FXView with FXChecks  with FXAlerts{

  protected val layout: String = LayoutRes.signUpLayout
  protected val title: String = StringRes.signUpTitle
  protected val stage: Stage = new Stage
  protected val controller: FXController = this

  @FXML
  private var tfUsername: TextField = _
  @FXML
  private var pfPassword: PasswordField = _
  @FXML
  private var pfPasswordConfirm: PasswordField = _


  @FXML
  private def onClickSignUp(): Unit = {
    Platform.runLater(() => {
      for(
        username <- getTextFieldValue(pfPassword, "È necessario inserire lo username");
        password <- getTextFieldValue(pfPassword, "È necessario inserire la password");
        passwordConfirm <- getTextFieldValue(pfPasswordConfirm, "È necessario inserire nuovamente la password")
        // TODO: controllare uguaglianza delle password
      ) yield strategy.onSignUp(username, password)
    })
  }

  @FXML
  private def onClickReset(): Unit = {
    Platform.runLater(() => {
      resetFields()
    })
  }

  override def resetFields(): Unit = {
    tfUsername setText ""
    pfPassword setText ""
    pfPasswordConfirm setText ""
  }
}
