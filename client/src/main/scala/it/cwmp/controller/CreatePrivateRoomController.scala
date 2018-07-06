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
  private var roomPassword: PasswordField = _
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

    })
  }

  @FXML
  private def onClickReset(): Unit = {
    Platform.runLater(() => {
      resetFields
    })
  }

  private def checkParam(): Boolean ={
    if(roomName.getText() != "" || roomPassword.getText() != ""){
      true
    }
    false
  }

  private def resetFields(): Unit = {
    roomName setText("")
    numPlayer getValueFactory() setValue 2
    roomPassword setText("")
    showPassword setSelected(false)
  }
}
