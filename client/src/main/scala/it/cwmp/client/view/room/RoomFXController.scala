package it.cwmp.client.view.room

import it.cwmp.client.utils.{LayoutRes, StringRes}
import it.cwmp.client.view.{FXChecks, FXController, FXView}
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.stage.Stage

trait RoomFXStrategy {
  def onCreate(name: String, nPlayer: Int): Unit
}

object RoomFXController {
  def apply(strategy: RoomFXStrategy): RoomFXController = {
    require(strategy != null)
    new RoomFXController(strategy)
  }
}

class RoomFXController(strategy: RoomFXStrategy) extends FXController with FXView with FXChecks {

  protected val layout: String = LayoutRes.roomManagerLayout
  protected val title: String = StringRes.roomManagerTitle
  protected val stage: Stage = new Stage
  protected val controller: FXController = this

  @FXML
  private var roomName: TextField = _
  @FXML
  private var numPlayer: Spinner[Integer] = _

  @FXML
  private def onClickCreate(): Unit = {
    Platform.runLater(() => {
      for(
        name <- getTextFieldValue(roomName, "Il nome non può essere vuoto");
        nPlayer <- getSpinnerFieldValue(numPlayer, "Deve essere selezionato il numero di giocatori")
      ) yield strategy.onCreate(name, nPlayer) // TODO correggere, discuterne con enry
    })
  }

  @FXML
  private def onClickReset(): Unit = {
    Platform.runLater(() => {
      resetFields()
    })
  }

  override def resetFields(): Unit = {
    roomName setText ""
    numPlayer getValueFactory() setValue 2
  }

}
