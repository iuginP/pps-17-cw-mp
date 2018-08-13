package it.cwmp.model

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.model.User.Converters._


/**
  * Test class for model class User.
  *
  * @author Elia Di Pasquale
  */
class UserTest extends ModelBaseTest("User") {

  private val userName = "general_name"
  private val user = User(userName)


  override protected def declarationInputTests(): Unit = assert(user.username == userName)

  override protected def declarationComplainTests(): Unit = {
    it("with an empty input")(
      // scalastyle:off null
      intercept[IllegalArgumentException](User(null)),
      // scalastyle:on null
      intercept[IllegalArgumentException](User("")),
      intercept[IllegalArgumentException](User("   "))
    )
  }

  override protected def conversionToJsonObjectTests(): Unit = {
    it("contains the parameter FIELD_USERNAME") {
      assert(user.toJson.containsKey(User.FIELD_USERNAME))
    }

    it("contains the same name value") {
      assert(user.toJson.getString(User.FIELD_USERNAME) == userName)
    }
  }

  override protected def conversionFromJsonObjectTests(): Unit = {

    it("should succeed if it contains the required parameter") {
      val correctJson: JsonObject = Json.obj((User.FIELD_USERNAME, userName))
      assert(correctJson.toUser.username == userName)
    }

    describe("should complain") {

      it("if the JsonObject is empty") {
        intercept[ParseException](Json.obj().toUser)
      }

      it("if it not contains the required parameter") {
        val wrongJson: JsonObject = Json.obj(("random_parameter", "random_value"))
        intercept[ParseException](wrongJson.toUser)
      }
    }
  }
}
