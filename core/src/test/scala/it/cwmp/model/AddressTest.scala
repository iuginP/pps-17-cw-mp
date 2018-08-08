package it.cwmp.model

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.model.Address.Converters._


/**
  * Test class for model class Address.
  *
  * @author Elia Di Pasquale
  */
class AddressTest extends ModelBaseTest("Address") {

  private val addressValue = "general_address"
  private val address = Address(addressValue)


  override protected def declarationInputTests(): Unit = assert(address.address == addressValue)

  override protected def declarationComplainTests(): Unit = {
    it("with an empty input")(
      // scalastyle:off null
      intercept[IllegalArgumentException](Address(null)),
      // scalastyle:on null
      intercept[IllegalArgumentException](Address("")),
      intercept[IllegalArgumentException](Address("   "))
    )
  }

  override protected def conversionToJsonObjectTests(): Unit = {
    it("contains the parameter FIELD_ADDRESS") {
      assert(address.toJson.containsKey(Address.FIELD_ADDRESS))
    }

    it("contains the same address value") {
      assert(address.toJson.getString(Address.FIELD_ADDRESS) == addressValue)
    }
  }

  override protected def conversionFromJsonObjectTests(): Unit = {

    it("should succeed if it contains the required parameter") {
      val correctJson: JsonObject = Json.obj((Address.FIELD_ADDRESS, addressValue))
      assert(correctJson.toAddress.address == addressValue)
    }

    describe("should complain") {

      it("if the JsonObject is empty") {
        intercept[ParseException](Json.obj().toAddress)
      }

      it("if it not contains the required parameter") {
        val wrongJson: JsonObject = Json.obj(("random_parameter", "random_value"))
        intercept[ParseException](wrongJson.toAddress)
      }
    }
  }
}
