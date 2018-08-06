package it.cwmp.model

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.model.User.Converters._
import org.scalatest.FunSpec


/**
  * Test class for model class Address
  *
  * @author Elia Di Pasquale
  */
class UserTest extends FunSpec {

  private val userName = "general_name"
  private val user = User(userName)


  describe("An user") {
    describe("on declaration") {

      it("should match the given input") {
        assert(user.username == userName)
      }

      describe("should complain") {
        it("with an empty input")(
          // scalastyle:off null
          intercept[IllegalArgumentException](User(null)),
          // scalastyle:on null
          intercept[IllegalArgumentException](User("")),
          intercept[IllegalArgumentException](User("   "))
        )
      }
    }

    describe("in case of conversion") {

      describe("the resulting JsonObject") {
        it("contains the parameter FIELD_USERNAME") {
          assert(user.toJson.containsKey(User.FIELD_USERNAME))
        }

        it("contains the same name value") {
          assert(user.toJson.getString(User.FIELD_USERNAME) == userName)
        }
      }

      describe("if it is obtained from a JsonObject") {

        val correctJson: JsonObject = Json.obj((User.FIELD_USERNAME, userName))
        it("should succeed if it contains the required parameter") {
          assert(correctJson.toUser.username == userName)
        }

        describe("should complain") {
          val wrongJson: JsonObject = Json.obj()

          it("if the JsonObject is empty") {
            intercept[ParseException](wrongJson.toUser)
          }

          wrongJson.put("random_parameter", "random_value")
          it("if it not contains the required parameter") {
            intercept[ParseException](wrongJson.toUser)
          }
        }
      }
    }
  }
}
