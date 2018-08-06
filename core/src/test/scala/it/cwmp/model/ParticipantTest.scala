package it.cwmp.model

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.model.Participant.Converters._
import org.scalatest.FunSpec


/**
  * Test class for model class Address
  *
  * @author Elia Di Pasquale
  */
class ParticipantTest extends FunSpec {

  private val nameValue = "general_name"
  private val addressValue = "general_address"
  private val participant = Participant(nameValue, addressValue)


  describe("A participant") {
    describe("on declaration") {

      it("should match the given input") {
        assert(participant.username == nameValue)
        assert(participant.address == addressValue)
      }

      describe("should complain") {
        it("with an empty input")(
          // scalastyle:off null
          intercept[IllegalArgumentException](Participant(null, null)),
          // scalastyle:on null
          intercept[IllegalArgumentException](Participant("", "")),
          intercept[IllegalArgumentException](Participant("   ", "   "))
        )
      }
    }

    describe("in case of conversion") {

      describe("the resulting JsonObject") {
        it("contains the right parameters") {
          assert(participant.toJson.containsKey(User.FIELD_USERNAME))
          assert(participant.toJson.containsKey(Address.FIELD_ADDRESS))
        }

        it("contains the same participant value") {
          assert(participant.toJson.getString(User.FIELD_USERNAME) == nameValue)
          assert(participant.toJson.getString(Address.FIELD_ADDRESS) == addressValue)
        }
      }

      describe("if it is obtained from a JsonObject") {

        val correctJson: JsonObject = Json.obj(
          (User.FIELD_USERNAME, nameValue),
          (Address.FIELD_ADDRESS, addressValue)
        )
        it("should succeed if it contains the required parameter") {
          assert(correctJson.toParticipant.username == nameValue)
          assert(correctJson.toParticipant.address == addressValue)
        }

        describe("should complain") {
          val wrongJson: JsonObject = Json.obj()

          it("if the JsonObject is empty") {
            intercept[ParseException](wrongJson.toParticipant)
          }

          wrongJson.put("random_parameter", "random_value")
          it("if it not contains the required parameter") {
            intercept[ParseException](wrongJson.toParticipant)
          }
        }
      }
    }
  }
}
