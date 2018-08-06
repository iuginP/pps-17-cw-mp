package it.cwmp.model

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}
import org.scalatest.FunSpec
import it.cwmp.model.Address.Converters._


/**
  * Test class for model class Address
  *
  * @author Elia Di Pasquale
  */
class AddressTest extends FunSpec {

  private val addressValue = "general_address"
  private val address = Address(addressValue)


  describe("An address") {
    describe("on declaration") {

      it("should match the given input") {
        assert(address.address == addressValue)
      }

      describe("should complain") {
        it("with an empty input")(
          // scalastyle:off null
          intercept[IllegalArgumentException](Address(null)),
          // scalastyle:on null
          intercept[IllegalArgumentException](Address("")),
          intercept[IllegalArgumentException](Address("   "))
        )
      }
    }

    describe("in case of conversion") {

      describe("the resulting JsonObject") {
        it("contains the parameter FIELD_ADDRESS") {
          assert(address.toJson.containsKey(Address.FIELD_ADDRESS))
        }

        it("contains the same address value") {
          assert(address.toJson.getString(Address.FIELD_ADDRESS) == addressValue)
        }
      }

      describe("if it is obtained from a JsonObject") {

        val correctJson: JsonObject = Json.obj((Address.FIELD_ADDRESS, addressValue))
        it("should succeed if it contains the required parameter") {
          assert(correctJson.toAddress.address == addressValue)
        }

        describe("should complain") {
          val wrongJson: JsonObject = Json.obj()

          it("if the JsonObject is empty") {
            intercept[ParseException](wrongJson.toAddress)
          }

          wrongJson.put("random_parameter", "random_value")
          it("if it not contains the required parameter") {
            intercept[ParseException](wrongJson.toAddress)
          }
        }
      }
    }
  }
}
