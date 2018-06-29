package it.cwmp.authentication

import org.scalatest.FunSpec
import pdi.jwt.JwtClaim

class JwtUtilsTest extends FunSpec {

  describe("Generic") {
    describe("encode") {
      it ("should succeed if right") {
        val claim = JwtClaim() + ("username", "tizio")
        assert(JwtUtils.encodeToken(claim).nonEmpty)
      }
      it ("should fail if argument null") {
        assert(JwtUtils.encodeToken(null).isEmpty)
      }
    }

    describe("decode") {
      it ("should succeed if right") {
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
      it ("should fail if argument null") {
        assert(JwtUtils.decodeToken(null).isEmpty)
      }
    }
  }
}
