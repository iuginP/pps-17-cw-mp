package it.cwmp.model

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.model.Room.Converters._
import org.scalatest.FunSpec


/**
  * Test class for model class Room
  *
  * @author Elia Di Pasquale
  */
class RoomTest extends FunSpec {

  private val idValue = "general_id"
  private val nameValue = "general_name"
  private val neededPlayers = 5

  private val room = Room(idValue, nameValue, neededPlayers, Seq[Participant]())


  describe("A room") {
    describe("on declaration") {

      it("should match the given input") {
        assert(room.identifier == idValue)
        assert(room.name == nameValue)
        assert(room.neededPlayersNumber == neededPlayers)
        assert(room.participants == Seq[Participant]())
      }

      describe("should complain") {
        it("with an empty roomID")(
          // scalastyle:off null
          intercept[IllegalArgumentException](Room(null, nameValue, neededPlayers, Seq[Participant]())),
          // scalastyle:on null
          intercept[IllegalArgumentException](Room("", nameValue, neededPlayers, Seq[Participant]())),
          intercept[IllegalArgumentException](Room("   ", nameValue, neededPlayers, Seq[Participant]()))
        )

        it("with an empty roomName")(
          // scalastyle:off null
          intercept[IllegalArgumentException](Room(idValue, null, neededPlayers, Seq[Participant]())),
          // scalastyle:on null
          intercept[IllegalArgumentException](Room(idValue, "", neededPlayers, Seq[Participant]())),
          intercept[IllegalArgumentException](Room(idValue, "   ", neededPlayers, Seq[Participant]()))
        )

        it("with less than one needed players")(
          intercept[IllegalArgumentException](Room(idValue, nameValue, 0, Seq[Participant]()))
        )
      }
    }

    describe("in case of conversion") {

      describe("the resulting JsonObject") {
        it("contains the right parameters") {
          assert(room.toJson.containsKey(Room.FIELD_IDENTIFIER))
          assert(room.toJson.containsKey(Room.FIELD_NAME))
          assert(room.toJson.containsKey(Room.FIELD_NEEDED_PLAYERS))
          assert(room.toJson.containsKey(Room.FIELD_PARTICIPANTS))
        }

        it("contains the same room values") {
          assert(room.toJson.getString(Room.FIELD_IDENTIFIER) == idValue)
          assert(room.toJson.getString(Room.FIELD_NAME) == nameValue)
          assert(room.toJson.getInteger(Room.FIELD_NEEDED_PLAYERS) == neededPlayers)
        }
      }

      describe("if it is obtained from a JsonObject") {

        val correctJson: JsonObject = Json.obj(
          (Room.FIELD_IDENTIFIER, idValue),
          (Room.FIELD_NAME, nameValue),
          (Room.FIELD_NEEDED_PLAYERS, neededPlayers),
          (Room.FIELD_PARTICIPANTS, Seq[Participant]())
        )
        it("should succeed if it contains the required parameter") {
          assert(correctJson.toRoom.identifier == idValue)
          assert(correctJson.toRoom.name == nameValue)
          assert(correctJson.toRoom.neededPlayersNumber == neededPlayers)
          assert(correctJson.toRoom.participants == Seq[Participant]())
        }

        describe("should complain") {
          val wrongJson: JsonObject = Json.obj()

          it("if the JsonObject is empty") {
            intercept[ParseException](wrongJson.toRoom)
          }

          wrongJson.put("random_parameter", "random_value")
          it("if it not contains the required parameter") {
            intercept[ParseException](wrongJson.toRoom)
          }
        }
      }
    }
  }
}
