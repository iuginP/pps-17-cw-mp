package it.cwmp.model

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.model.Participant.Converters._
import org.scalatest.FunSpec


/**
  * Test class for model class Participant.
  *
  * @author Elia Di Pasquale
  */
class ParticipantTest extends ModelBaseTest("Participant") {

  private val nameValue = "general_name"
  private val addressValue = "general_address"
  private val participant = Participant(nameValue, addressValue)


  override protected def declarationInputTests(): Unit = {
    assert(participant.username == nameValue)
    assert(participant.address == addressValue)
  }

  override protected def declarationComplainTests(): Unit = {
    it("with an empty input")(
      // scalastyle:off null
      intercept[IllegalArgumentException](Participant(null, null)),
      // scalastyle:on null
      intercept[IllegalArgumentException](Participant("", "")),
      intercept[IllegalArgumentException](Participant("   ", "   "))
    )
  }

  override protected def conversionToJsonObjectTests(): Unit = {
    it("contains the right parameters") {
      assert(participant.toJson.containsKey(User.FIELD_USERNAME))
      assert(participant.toJson.containsKey(Address.FIELD_ADDRESS))
    }

    it("contains the same participant value") {
      assert(participant.toJson.getString(User.FIELD_USERNAME) == nameValue)
      assert(participant.toJson.getString(Address.FIELD_ADDRESS) == addressValue)
    }
  }

  override protected def conversionFromJsonObjectTests(): Unit = {

    it("should succeed if it contains the required parameter") {
      val correctJson: JsonObject = Json.obj(
        (User.FIELD_USERNAME, nameValue),
        (Address.FIELD_ADDRESS, addressValue)
      )
      assert(correctJson.toParticipant.username == nameValue)
      assert(correctJson.toParticipant.address == addressValue)
    }

    describe("should complain") {
      it("if the JsonObject is empty") {
        intercept[ParseException](Json.obj().toParticipant)
      }

      it("if it not contains the required parameter") {
        val wrongJson: JsonObject = Json.obj()
        wrongJson.put("random_parameter", "random_value")
        intercept[ParseException](wrongJson.toParticipant)
      }
    }
  }
}
