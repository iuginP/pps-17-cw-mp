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

  def participants: Seq[User]
}

/**
  * Companion object
  *
  * @author Enrico Siboni
  */
object Room {

  def apply(roomID: String,
            roomName: String,
            neededPlayersNumber: Int,
            participants: Seq[User] = Seq()): Room = {

    if (roomID.isEmpty) throw new IllegalArgumentException("Room ID empty")
    if (roomName.isEmpty) throw new IllegalAccessException("Room name empty")
    if (neededPlayersNumber < 1) throw new IllegalArgumentException("Room needed players less than one")

    RoomDefault(roomID, roomName, neededPlayersNumber, participants)
  }

  def unapply(toExtract: Room): Option[(String, String, Int, Seq[User])] =
    Some(toExtract.identifier, toExtract.name, toExtract.neededPlayersNumber, toExtract.participants)

  /**
    * Default implementation for Room
    *
    * @author Enrico Siboni
    */
  private case class RoomDefault(identifier: String,
                                 name: String,
                                 neededPlayersNumber: Int,
                                 participants: Seq[User]) extends Room


  val FIELD_IDENTIFIER = "room_identifier"
  val FIELD_NAME = "room_name"
  val FIELD_NEEDED_PLAYERS = "room_needed_players"
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

    implicit class JsonRoomConverter(jsonObject: JsonObject) {
      def toRoom: Room = {
        if (jsonObject.containsKey(FIELD_IDENTIFIER) && jsonObject.containsKey(FIELD_NAME)
          && jsonObject.containsKey(FIELD_NEEDED_PLAYERS) && jsonObject.containsKey(FIELD_PARTICIPANTS)) {

          var userSeq = Seq[User]()
          import User.Converters._
          jsonObject.getJsonArray(FIELD_PARTICIPANTS).getList.forEach(jsonUser => {
            userSeq = Json.fromObjectString(jsonUser.toString).toUser +: userSeq
          })

          Room(
            jsonObject.getString(FIELD_IDENTIFIER),
            jsonObject.getString(FIELD_NAME),
            jsonObject.getInteger(FIELD_NEEDED_PLAYERS),
            userSeq)
        } else {
          throw new ParseException(s"The input doesn't contain $FIELD_NAME, $FIELD_PARTICIPANTS --> ${jsonObject.encodePrettily()}", 0)
        }
      }
    }

  }

}