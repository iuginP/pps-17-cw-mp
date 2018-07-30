package it.cwmp.services.authentication

import org.scalatest.FunSpec
import pdi.jwt.JwtClaim

/**
  * A test class for JwtUtils
  */
class JwtUtilsTest extends FunSpec {

  describe("Generic") {
    describe("encode") {
      it("should succeed if right") {
        val claim = JwtClaim() + ("username", "tizio")
        assert(JwtUtils.encodeToken(claim).nonEmpty)
      }
      it("should fail if argument null") {
        assert(JwtUtils.encodeToken(null).isEmpty)
      }
    }

    describe("decode") {
      it("should succeed if right") {
        val claim = JwtClaim() + ("username", "tizio")
        var result: Option[JwtClaim] = None
        for (
          token <- JwtUtils.encodeToken(claim);
          decoded <- JwtUtils.decodeToken(token)
        ) yield {
          result = Some(decoded)
        }
        assert(result.isDefined && result.get.content.nonEmpty)
      }
      it("should fail if argument null") {
        assert(JwtUtils.decodeToken(null).isEmpty)
      }
    }

    describe("validate") {
      it("should succeed if right") {
        val claim = JwtClaim() + ("username", "tizio")
        var result = false
        for (
          token <- JwtUtils.encodeToken(claim);
          isValid = JwtUtils.validateToken(token)
        ) yield {
          result = isValid
        }
        assert(result)
      }
      it("should fail if argument null") {
        assert(!JwtUtils.validateToken(null))
      }
    }
  }

  describe("User specific") {
    describe("encode") {
      it("should succeed if right") {
        assert(JwtUtils.encodeUsernameToken("username").nonEmpty)
      }
      it("should fail if argument null") {
        assert(JwtUtils.encodeUsernameToken(null).isEmpty)
      }
    }

    describe("decode") {
      it("should succeed if right") {
        var result: Option[String] = None
        for (
          token <- JwtUtils.encodeUsernameToken("username");
          decoded <- JwtUtils.decodeUsernameToken(token)
        ) yield {
          result = Some(decoded)
        }
        assert(result.isDefined && result.get == "username")
      }
      it("should fail if argument null") {
        assert(JwtUtils.decodeUsernameToken(null).isEmpty)
      }
    }
  }
}
