package it.cwmp.client.view.room

import it.cwmp.client.utils.{LayoutRes, StringRes}
import it.cwmp.client.view._
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.stage.Stage

trait RoomFXStrategy {
  def onCreate(name: String, nPlayer: Int): Unit
  def onEnterPrivate(idRoom: String): Unit
  def onEnterPublic(nPlayer: Int): Unit
}

object RoomFXController {
  def apply(strategy: RoomFXStrategy): RoomFXController = {
    require(strategy != null)
    new RoomFXController(strategy)
  }
}

class RoomFXController(strategy: RoomFXStrategy) extends FXController with FXView with FXChecks with FXAlerts with FXLoadingDialog{

  protected val layout: String = LayoutRes.roomManagerLayout
  protected val title: String = StringRes.roomManagerTitle
  protected val stage: Stage = new Stage
  protected val controller: FXController = this

  @FXML
  private var tfPrivateCreateRoomName: TextField = _
  @FXML
  private var spPrivateCreateNumPlayer: Spinner[Integer] = _
  @FXML
  private var tfPrivateEnterRoomID: TextField = _
  @FXML
  private var spPublicEnterNumPlayer: Spinner[Integer] = _
  @FXML
  private var btnPrivateCreate: Button = _
  @FXML
  private var btnPrivateReset: Button = _
  @FXML
  private var btnPrivateEnter: Button = _
  @FXML
  private var btnPublicEnter: Button = _

  //creare una stanza privata
  @FXML
  private def onClickCreate(): Unit = {
    Platform.runLater(() => {
      for(
        name <- getTextFieldValue(tfPrivateCreateRoomName, "Il nome non può essere vuoto"); // TODO parametrize input
        nPlayer <- getSpinnerFieldValue(spPrivateCreateNumPlayer, "Deve essere selezionato il numero di giocatori")
      ) yield{
        showLoadingDialog("Loading", "Stiamo creando la stanza privata")
        strategy.onCreate(name, nPlayer)
        btnPrivateCreate.setDisable(true)
        btnPrivateReset.setDisable(true)
      }
    })
  }

  @FXML
  private def onClickReset(): Unit = {
    Platform.runLater(() => {
      resetFields()
    })
  }

  override def resetFields(): Unit = {
    tfPrivateCreateRoomName setText ""
    spPrivateCreateNumPlayer getValueFactory() setValue 2
  }

  @FXML
  private def onClickEnter(): Unit = {
    Platform.runLater(() => {
      for(
        id_room <- getTextFieldValue(tfPrivateEnterRoomID, "L'ID della stanza non può essere vuoto") // TODO parametrize input
      ) yield {
        showLoadingDialog("Loading", "Stai per entrare nella stanza privata")
        strategy.onEnterPrivate(id_room)
        btnPrivateEnter.setDisable(true)
      }
    })
  }

  //Componenti tab stanze pubbliche
  @FXML
  private def onClickRoomPublic(): Unit = {
    Platform.runLater(() => {
      for(
        nPlayer <- getSpinnerFieldValue(spPublicEnterNumPlayer, "Deve essere selezionato il numero di giocatori") // TODO parametrize input
      ) yield {
        showLoadingDialog("Loading", "Stai per entrare in una stanza pubblica")
        strategy.onEnterPublic(nPlayer)
        btnPublicEnter.setDisable(true)
      }
    })
  }

  override def enableButtons(): Unit = {
    btnPrivateCreate.setDisable(false)
    btnPrivateReset.setDisable(false)
    btnPrivateEnter.setDisable(false)
    btnPublicEnter.setDisable(false)
  }
}
