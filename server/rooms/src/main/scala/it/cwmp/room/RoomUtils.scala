package it.cwmp.room

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.model.{Room, User}

/**
  * Utilities to manage Rooms
  */
object RoomUtils {

  val FIELD_ROOM_NAME = "room_name"
  val FIELD_ROOM_PARTICIPANTS = "room_participants"

  implicit class RichRoom(room: Room) {
    def toJson: JsonObject = {
      Json.obj(
        (FIELD_ROOM_NAME, room.name),
        (FIELD_ROOM_PARTICIPANTS, for (user <- room.participants) yield user.username))
    }
  }

  implicit class JsonRoomConverter(jsonObject: JsonObject) {
    def toRoom: Room = {
      if (jsonObject.containsKey(FIELD_ROOM_NAME) && jsonObject.containsKey(FIELD_ROOM_PARTICIPANTS)) {
        var userSeq = Seq[User]()
        jsonObject.getJsonArray(FIELD_ROOM_PARTICIPANTS).getList.forEach(username => {
          userSeq = User(username.toString) +: userSeq
        })

        Room(jsonObject.getString(FIELD_ROOM_NAME), userSeq)
      } else {
        throw new ParseException(s"The input doesn't contain $FIELD_ROOM_NAME, $FIELD_ROOM_PARTICIPANTS --> ${jsonObject.encodePrettily()}", 0)
      }
    }
  }

}