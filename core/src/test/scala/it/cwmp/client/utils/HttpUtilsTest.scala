package it.cwmp.client.utils

import org.scalatest.FunSpec

class HttpUtilsTest extends  FunSpec{

  describe("Basic Authentication") {
    describe("build") {
      it("should succeed if right") {
        assert(HttpUtils.buildBasicAuthentication("pippo", "pluto").nonEmpty)
      }
      it("should fail if argument is empty") {
        assert(HttpUtils.buildBasicAuthentication("", "").isEmpty)
      }
      it("should fail if argument is null") {
        assert(HttpUtils.buildBasicAuthentication(null, null).isEmpty)
      }
    }

    describe("read") {
      it ("should succeed if right") {
        val username = "pippo"
        val password = "password"
        var result: Option[(String, String)] = None
        for (
          token <- HttpUtils.buildBasicAuthentication(username, password);
          decoded = HttpUtils.readBasicAuthentication(token)
        ) yield {
          result = decoded
        }
        result match {
          case Some((u, p)) => assert(u == username && p == password )
          case _ => fail
        }
      }
      it ("should fail if argument is empty") {
        assert(HttpUtils.readBasicAuthentication("").isEmpty)
      }
      it ("should fail if argument is null") {
        assert(HttpUtils.readBasicAuthentication(null).isEmpty)
      }
    }
  }

  describe("JWT authentication") {
    describe("build") {
      it("should succeed if right") {
        assert(HttpUtils.buildJwtAuthentication("token").nonEmpty)
      }
      it("should fail if argument is empty") {
        assert(HttpUtils.buildJwtAuthentication("").isEmpty)
      }
      it("should fail if argument is null") {
        assert(HttpUtils.buildJwtAuthentication(null).isEmpty)
      }
    }

    describe("read") {
      it ("should succeed if right") {
        var result: Option[String] = None
        val myToken = "token"
        for (
          token <- HttpUtils.buildJwtAuthentication(myToken);
          decoded = HttpUtils.readJwtAuthentication(token)
        ) yield {
          result = decoded
        }
        result match {
          case Some((t)) => assert(t == myToken )
          case _ => fail
        }
      }
      it ("should fail if argument is empty") {
        assert(HttpUtils.readJwtAuthentication("").isEmpty)
      }
      it ("should fail if argument is null") {
        assert(HttpUtils.readJwtAuthentication(null).isEmpty)
      }
    }
  }
}