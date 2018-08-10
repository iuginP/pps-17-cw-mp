package it.cwmp.services.authentication.utils

import org.scalatest.FunSpec
import pdi.jwt.JwtClaim

/**
  * A test class for JwtUtils
  */
class JwtUtilsTest extends FunSpec {

  private val myUsername = "Bobby"

  describe("Generic") {
    describe("Encode") {
      it("should succeed if parameters correct") {
        val claim = JwtClaim() + (JwtUtils.USERNAME_FIELD_NAME, myUsername)
        assert(JwtUtils.encodeToken(claim).nonEmpty)
      }
      it("should fail with wrong argument") {
        // scalastyle:off null
        assert(JwtUtils.encodeToken(null).isEmpty)
        // scalastyle:on null
      }
    }

    describe("Decode") {
      it("should succeed if right parameters") {
        val claim = JwtClaim() + (JwtUtils.USERNAME_FIELD_NAME, myUsername)
        val result: Option[JwtClaim] =
          for (
            token <- JwtUtils.encodeToken(claim);
            decoded <- JwtUtils.decodeToken(token)
          ) yield decoded

        assert(result.isDefined && result.get.content.nonEmpty)
      }
      it("should fail if argument is null") {
        // scalastyle:off null
        assert(JwtUtils.decodeToken(null).isEmpty)
        // scalastyle:on null
      }
    }

    describe("validate") {
      it("should succeed if arguments correct") {
        val claim = JwtClaim() + (JwtUtils.USERNAME_FIELD_NAME, myUsername)
        val validationResult =
          for (
            token <- JwtUtils.encodeToken(claim);
            isValid = JwtUtils.validateToken(token)
          ) yield isValid

        assert(validationResult.getOrElse(false))
      }
      it("should fail if argument was null") {
        // scalastyle:off null
        assert(!JwtUtils.validateToken(null))
        // scalastyle:on null
      }
    }
  }

  describe("User specific") {
    describe("Encode") {
      it("should succeed if right") {
        assert(JwtUtils.encodeUsernameToken(myUsername).nonEmpty)
      }
      it("should fail if argument null") {
        // scalastyle:off null
        assert(JwtUtils.encodeUsernameToken(null).isEmpty)
        // scalastyle:on null
      }
    }

    describe("Decode") {
      it("should succeed if right") {
        val result: Option[String] =
          for (
            token <- JwtUtils.encodeUsernameToken(myUsername);
            decoded <- JwtUtils.decodeUsernameToken(token)
          ) yield decoded

        assert(result.isDefined && result.get == myUsername)
      }
      it("should fail if argument null") {
        // scalastyle:off null
        assert(JwtUtils.decodeUsernameToken(null).isEmpty)
        // scalastyle:on null
      }
    }
  }
}
