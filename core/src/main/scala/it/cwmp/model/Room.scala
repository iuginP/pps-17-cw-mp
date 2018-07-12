package it.cwmp.model

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.utils.Utils._

/**
  * Trait that describes the Room
  *
  * @author Enrico Siboni
  */
sealed trait Room {
  def identifier: String

  def name: String

  def neededPlayersNumber: Int

  def participants: Seq[Participant]
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
            participants: Seq[Participant] = Seq()): Room = {

    require(!emptyString(roomID), "Room ID empty")
    require(!emptyString(roomName), "Room name empty")
    if (neededPlayersNumber < 1) throw new IllegalArgumentException("Room needed players less than one")

    RoomDefault(roomID, roomName, neededPlayersNumber, participants)
  }

  def unapply(toExtract: Room): Option[(String, String, Int, Seq[Participant])] =
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
                                 participants: Seq[Participant]) extends Room


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
        import Participant.Converters._
        Json.obj(
          (FIELD_IDENTIFIER, room.identifier),
          (FIELD_NAME, room.name),
          (FIELD_NEEDED_PLAYERS, room.neededPlayersNumber),
          (FIELD_PARTICIPANTS, for (participant <- room.participants) yield participant.toJson)
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

          var userSeq = Seq[Participant]()
          jsonObject.getJsonArray(FIELD_PARTICIPANTS) forEach (jsonUser => {
            import Participant.Converters._
            userSeq = Json.fromObjectString(jsonUser.toString).toParticipant +: userSeq
          })

          Room(
            jsonObject getString FIELD_IDENTIFIER,
            jsonObject getString FIELD_NAME,
            jsonObject getInteger FIELD_NEEDED_PLAYERS,
            userSeq)
        } else {
          throw parseException("Room Parsing", s"The input doesn't contain one or more of: " +
            s"$FIELD_IDENTIFIER, $FIELD_NAME, $FIELD_NEEDED_PLAYERS, $FIELD_PARTICIPANTS --> ${jsonObject.encodePrettily()}")
        }
      }
    }

  }

}