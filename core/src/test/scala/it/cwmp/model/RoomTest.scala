package it.cwmp.model

import java.text.ParseException

import it.cwmp.model.Room.Converters._
import io.vertx.lang.scala.json.{Json, JsonObject}


/**
  * Test class for model class Room.
  *
  * @author Elia Di Pasquale
  */
class RoomTest extends ModelBaseTest("Room") {

  private val NEEDED_PLAYER = 5

  private val idValue = "general_id"
  private val nameValue = "general_name"

  private val room = Room(idValue, nameValue, NEEDED_PLAYER, Seq[Participant]())


  override protected def declarationInputTests(): Unit = {
    assert(room.identifier == idValue)
    assert(room.name == nameValue)
    assert(room.neededPlayersNumber == NEEDED_PLAYER)
    assert(room.participants == Seq[Participant]())
  }

  override protected def declarationComplainTests(): Unit = {
    it("with an empty roomID")(
      // scalastyle:off null
      intercept[IllegalArgumentException](Room(null, nameValue, NEEDED_PLAYER, Seq[Participant]())),
      // scalastyle:on null
      intercept[IllegalArgumentException](Room("", nameValue, NEEDED_PLAYER, Seq[Participant]())),
      intercept[IllegalArgumentException](Room("   ", nameValue, NEEDED_PLAYER, Seq[Participant]()))
    )

    it("with an empty roomName")(
      // scalastyle:off null
      intercept[IllegalArgumentException](Room(idValue, null, NEEDED_PLAYER, Seq[Participant]())),
      // scalastyle:on null
      intercept[IllegalArgumentException](Room(idValue, "", NEEDED_PLAYER, Seq[Participant]())),
      intercept[IllegalArgumentException](Room(idValue, "   ", NEEDED_PLAYER, Seq[Participant]()))
    )

    it("with less than one needed players")(
      intercept[IllegalArgumentException](Room(idValue, nameValue, 0, Seq[Participant]()))
    )
  }

  override protected def conversionToJsonObjectTests(): Unit = {
    it("contains the right parameters") {
      assert(room.toJson.containsKey(Room.FIELD_IDENTIFIER))
      assert(room.toJson.containsKey(Room.FIELD_NAME))
      assert(room.toJson.containsKey(Room.FIELD_NEEDED_PLAYERS))
      assert(room.toJson.containsKey(Room.FIELD_PARTICIPANTS))
    }

    it("contains the same room values") {
      assert(room.toJson.getString(Room.FIELD_IDENTIFIER) == idValue)
      assert(room.toJson.getString(Room.FIELD_NAME) == nameValue)
      assert(room.toJson.getInteger(Room.FIELD_NEEDED_PLAYERS) == NEEDED_PLAYER)
    }
  }

  override protected def conversionFromJsonObjectTests(): Unit = {
    it("should succeed if it contains the required parameter") {
      val correctJson: JsonObject = Json.obj(
        (Room.FIELD_IDENTIFIER, idValue),
        (Room.FIELD_NAME, nameValue),
        (Room.FIELD_NEEDED_PLAYERS, NEEDED_PLAYER),
        (Room.FIELD_PARTICIPANTS, Seq[Participant]())
      )

      assert(correctJson.toRoom.identifier == idValue)
      assert(correctJson.toRoom.name == nameValue)
      assert(correctJson.toRoom.neededPlayersNumber == NEEDED_PLAYER)
      assert(correctJson.toRoom.participants == Seq[Participant]())
    }

    describe("should complain") {
      it("if the JsonObject is empty") {
        intercept[ParseException](Json.obj().toRoom)
      }

      it("if it not contains the required parameter") {
        val wrongJson: JsonObject = Json.obj(("random_parameter", "random_value"))
        intercept[ParseException](wrongJson.toRoom)
      }
    }
  }
}
