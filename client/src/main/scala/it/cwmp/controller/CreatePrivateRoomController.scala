package it.cwmp.controller

import javafx.application.Platform
import javafx.scene.control.{CheckBox, PasswordField, Spinner, TextField}
import javafx.fxml.FXML
import javafx.stage.Stage

class CreatePrivateRoomController extends ViewController{

  var stage: Stage = _

  @FXML
  private var roomName: TextField = _
  @FXML
  private var numPlayer: Spinner[Integer] = _
  @FXML
  private var pass_hidden: PasswordField = _
  @FXML
  private var plain_password: TextField = _
  @FXML
  private var showPassword: CheckBox = _


  @FXML
  private def onClickCreate(): Unit = {
    Platform.runLater(() => {
      if (checkParam()){
        //todo fare richiesta
      }
    })
  }

  @FXML
  private def onChecked(): Unit = {
    Platform.runLater(() => {
      showHidePassword()
    })
  }

  @FXML
  private def onClickReset(): Unit = {
    Platform.runLater(() => {
      resetFields
    })
  }

  private def checkParam(): Boolean ={
    if(roomName.getText() != "" || pass_hidden.getText() != ""){
      true
    }
    false
  }

  def showHidePassword(): Unit = {
    if (showPassword.isSelected) {
      plain_password.setText(pass_hidden.getText)
      plain_password.setVisible(true)
      pass_hidden.setVisible(false)
      return
    }
    pass_hidden.setText(plain_password.getText)
    pass_hidden.setVisible(true)
    plain_password.setVisible(false)
  }

  private def resetFields(): Unit = {
    roomName setText("")
    numPlayer getValueFactory() setValue 2
    pass_hidden setText("")
    showPassword setSelected(false)
  }
}
