package it.cwmp.model

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}

/**
  * Trait that describes the Room
  *
  * @author Enrico Siboni
  */
sealed trait Room {
  def identifier: String

  def name: String

  def neededPlayersNumber: Int

  def participants: Seq[User with Address]
}

/**
  * Companion object
  *
  * @author Enrico Siboni
  */
object Room {

  import it.cwmp.utils.Utils.parameterEmptyCheck

  def apply(roomID: String,
            roomName: String,
            neededPlayersNumber: Int,
            participants: Seq[User with Address] = Seq()): Room = {

    parameterEmptyCheck(roomID, "Room ID empty")
    parameterEmptyCheck(roomName, "Room name empty")
    if (neededPlayersNumber < 1) throw new IllegalArgumentException("Room needed players less than one")

    RoomDefault(roomID, roomName, neededPlayersNumber, participants)
  }

  def unapply(toExtract: Room): Option[(String, String, Int, Seq[User with Address])] =
    if (toExtract eq null) None
    else Some(toExtract.identifier, toExtract.name, toExtract.neededPlayersNumber, toExtract.participants)

  /**
    * Default implementation for Room
    *
    * @author Enrico Siboni
    */
  private case class RoomDefault(identifier: String,
                                 name: String,
                                 neededPlayersNumber: Int,
                                 participants: Seq[User with Address]) extends Room


  val FIELD_IDENTIFIER = "room_identifier"
  val FIELD_NAME = "room_name"
  val FIELD_NEEDED_PLAYERS = "room_players"
  val FIELD_PARTICIPANTS = "room_participants"

  /**
    * Converters for Room
    *
    * @author Enrico Siboni
    */
  object Converters {

    /**
      * A class that enables Room to be converted to Json
      *
      * @param room the room to convert
      */
    implicit class RichRoom(room: Room) {
      def toJson: JsonObject = {
        import User.Converters._
        Json.obj(
          (FIELD_IDENTIFIER, room.identifier),
          (FIELD_NAME, room.name),
          (FIELD_NEEDED_PLAYERS, room.neededPlayersNumber),
          (FIELD_PARTICIPANTS, for (user <- room.participants) yield user.toJson)
        )
      }
    }

    /**
      * Json to Room Converter
      */
    implicit class JsonRoomConverter(jsonObject: JsonObject) {
      def toRoom: Room = {
        if ((jsonObject containsKey FIELD_IDENTIFIER) && (jsonObject containsKey FIELD_NAME)
          && (jsonObject containsKey FIELD_NEEDED_PLAYERS) && (jsonObject containsKey FIELD_PARTICIPANTS)) {

          var userSeq = Seq[User with Address]()
          jsonObject.getJsonArray(FIELD_PARTICIPANTS).getList forEach (jsonUser => {
            import User.Converters._
            userSeq = Json.fromObjectString(jsonUser.toString).toUserWithAddress +: userSeq
          })

          Room(
            jsonObject getString FIELD_IDENTIFIER,
            jsonObject getString FIELD_NAME,
            jsonObject getInteger FIELD_NEEDED_PLAYERS,
            userSeq)
        } else {
          throw new ParseException(s"The input doesn't contain one or more of: " +
            s"$FIELD_IDENTIFIER, $FIELD_NAME, $FIELD_NEEDED_PLAYERS, $FIELD_PARTICIPANTS --> ${jsonObject.encodePrettily()}", 0)
        }
      }
    }

  }

}