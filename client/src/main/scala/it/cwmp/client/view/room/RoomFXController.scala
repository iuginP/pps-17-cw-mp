package it.cwmp.client.view.room

import it.cwmp.client.utils.LayoutRes
import it.cwmp.client.view.{FXAlerts, FXChecks, FXController, FXView}
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.stage.Stage

trait RoomFXStrategy {
  def onCreate(name: String, nPlayer: Int): Unit
  def onEnterPrivate(idRoom: String): Unit
}

object RoomFXController {
  def apply(strategy: RoomFXStrategy): RoomFXController = {
    require(strategy != null)
    new RoomFXController(strategy)
  }
}

class RoomFXController(strategy: RoomFXStrategy) extends FXController with FXView with FXChecks with FXAlerts {

  protected val layout: String = LayoutRes.roomManagerLayout
  protected val stage: Stage = new Stage
  protected val controller: FXController = this

  @FXML
  private var pr_cr_roomName: TextField = _
  @FXML
  private var pr_cr_numPlayer: Spinner[Integer] = _
  @FXML
  private var pr_et_roomID: TextField = _

  //creare una stanza privata
  @FXML
  private def onClickCreate(): Unit = {
    Platform.runLater(() => {
      for(
        name <- getTextFieldValue(pr_cr_roomName, "Il nome non può essere vuoto"); // TODO parametrize input
        nPlayer <- getSpinnerFieldValue(pr_cr_numPlayer, "Deve essere selezionato il numero di giocatori")
      ) yield strategy.onCreate(name, nPlayer)
    })
  }

  @FXML
  private def onClickReset(): Unit = {
    Platform.runLater(() => {
      resetFields()
    })
  }

  override def resetFields(): Unit = {
    pr_cr_roomName setText ""
    pr_cr_numPlayer getValueFactory() setValue 2
  }

  @FXML
  private def onClickEnter(): Unit = {
    Platform.runLater(() => {
      for(
        id_room <- getTextFieldValue(pr_et_roomID, "L'ID della stanza non può essere vuoto") // TODO parametrize input
      ) yield strategy.onEnterPrivate(id_room)
    })
  }

  //Componenti tab stanze pubbliche
  @FXML
  private def onClickRoomTwoPlayers(): Unit = ???
  @FXML
  private def onClickRoomThreePlayers(): Unit = ???
  @FXML
  private def onClickRoomFourPlayers(): Unit = ???

}
