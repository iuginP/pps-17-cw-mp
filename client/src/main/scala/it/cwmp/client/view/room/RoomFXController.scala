package it.cwmp.client.view.room

import it.cwmp.client.utils.{LayoutRes, StringRes}
import it.cwmp.client.view._
import it.cwmp.client.view.room.RoomFXController._
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.input.{Clipboard, ClipboardContent}
import javafx.scene.layout.GridPane

/**
  * Class that manages the Rooms View
  *
  * @param strategy the strategy to use on user actions
  * @author contributor Enrico Siboni
  */
class RoomFXController(strategy: RoomStrategy) extends FXViewController with FXInputViewController with FXInputChecks {

  protected val layout: String = LayoutRes.roomManagerLayout
  protected val title: String = StringRes.roomManagerTitle
  protected val controller: FXViewController = this

  @FXML private var tabPane: TabPane = _
  @FXML private var tfPrivateCreateRoomName: TextField = _
  @FXML private var spPrivateCreateNumPlayer: Spinner[Int] = _
  @FXML private var tfPrivateEnterRoomID: TextField = _
  @FXML private var spPublicEnterNumPlayer: Spinner[Int] = _
  @FXML private var btnPrivateCreate: Button = _
  @FXML private var btnPrivateReset: Button = _
  @FXML private var btnPrivateEnter: Button = _
  @FXML private var btnPublicEnter: Button = _

  override def showGUI(): Unit = {
    super.showGUI()
    // adds a listener to reset fields on tab change
    tabPane.getSelectionModel.selectedItemProperty.addListener((_, _, _) => resetFields())
  }

  override def resetFields(): Unit = {
    tfPrivateCreateRoomName setText ""
    spPrivateCreateNumPlayer getValueFactory() setValue 2
    tfPrivateEnterRoomID setText ""
    spPublicEnterNumPlayer getValueFactory() setValue 2
  }

  override def disableViewComponents(): Unit = {
    btnPrivateCreate.setDisable(true)
    btnPrivateReset.setDisable(true)
    btnPrivateEnter.setDisable(true)
    btnPublicEnter.setDisable(true)
  }

  override def enableViewComponents(): Unit = {
    btnPrivateCreate.setDisable(false)
    btnPrivateReset.setDisable(false)
    btnPrivateEnter.setDisable(false)
    btnPublicEnter.setDisable(false)
  }

  @FXML private def onClickCreatePrivate(): Unit =
    runOnUIThread(() => {
      for (
        roomName <- getTextFieldValue(tfPrivateCreateRoomName, ROOM_NAME_EMPTY_ERROR);
        playersNumber <- getSpinnerFieldValue(spPrivateCreateNumPlayer, ROOM_PLAYERS_NUMBER_ERROR)
      ) yield
        strategy.onCreate(roomName, playersNumber)
    })

  @FXML private def onClickResetPrivate(): Unit = runOnUIThread { () => resetFields() }

  @FXML private def onClickEnterPrivate(): Unit =
    runOnUIThread(() => {
      for (roomID <- getTextFieldValue(tfPrivateEnterRoomID, EMPTY_ROOM_ID_ERROR))
        yield strategy.onEnterPrivate(roomID)
    })


  @FXML private def onClickEnterPublic(): Unit =
    runOnUIThread(() => {
      for (playersNumber <- getSpinnerFieldValue(spPublicEnterNumPlayer, NOT_SELECTED_PLAYERS_NUMBER))
        yield strategy.onEnterPublic(playersNumber)
    })


  /**
    * A method to show the token dialog to user
    *
    * @param roomToken the token that user should be able to copy
    */
  def showTokenDialog(roomToken: String): Unit =
    runOnUIThread(() =>
      showInfoWithContent(PRIVATE_ROOM_TOKEN_TITLE, PRIVATE_ROOM_TOKEN_MESSAGE,
        createRoomTokenDialogContent(roomToken)))

  /**
    * Creates the dialog content whit selectable token
    *
    * @param roomToken the token to show
    * @return the content of the dialog to show
    */
  private def createRoomTokenDialogContent(roomToken: String): Node = {
    val gridPane = new GridPane()

    val tokenTextField = new TextField(roomToken)
    tokenTextField.setEditable(false)

    val copyButton = new Button(COPY_BUTTON_TEXT)
    copyButton.setOnAction(_ => {
      val content = new ClipboardContent
      content.putString(roomToken)
      Clipboard.getSystemClipboard.setContent(content)
    })

    gridPane.add(tokenTextField, 0, 1)
    gridPane.add(copyButton, 1, 1)
    gridPane
  }
}

/**
  * Companion object
  */
object RoomFXController {
  def apply(strategy: RoomStrategy): RoomFXController = {
    require(strategy != null, "The room strategy cannot be null")
    new RoomFXController(strategy)
  }

  private val ROOM_NAME_EMPTY_ERROR = "Il nome della stanza non può essere vuoto"
  private val ROOM_PLAYERS_NUMBER_ERROR = "Deve essere selezionato il numero di giocatori"
  private val EMPTY_ROOM_ID_ERROR = "L'ID della stanza non può essere vuoto"
  private val NOT_SELECTED_PLAYERS_NUMBER = "Deve essere selezionato il numero di giocatori"

  private val PRIVATE_ROOM_TOKEN_TITLE = "Token per la stanza privata"

  private val PRIVATE_ROOM_TOKEN_MESSAGE = "Questo è il token da usare per entrare nella stanza che hai creato"

  private val COPY_BUTTON_TEXT = "Copy to clipboard"
}
