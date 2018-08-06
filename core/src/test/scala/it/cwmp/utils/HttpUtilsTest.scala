package it.cwmp.utils

import org.scalatest.FunSpec

/**
  * A test class for HttpUtils
  */
class HttpUtilsTest extends FunSpec {

  describe("Basic Authentication") {
    describe("Build") {
      it("should succeed if input correct") {
        assert(HttpUtils.buildBasicAuthentication("pippo", "pluto").nonEmpty)
      }
      it("should fail if arguments empty") {
        assert(HttpUtils.buildBasicAuthentication("", "").isEmpty)
      }
      it("should fail if arguments null") {
        // scalastyle:off null
        assert(HttpUtils.buildBasicAuthentication(null, null).isEmpty)
        // scalastyle:on null
      }
    }

    describe("Read") {
      it("should succeed if argument valid") {
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
          case Some((u, p)) => assert(u == username && p == password)
          case _ => fail
        }
      }
      it("should fail if argument empty") {
        assert(HttpUtils.readBasicAuthentication("").isEmpty)
      }
      it("should fail if argument null") {
        // scalastyle:off null
        assert(HttpUtils.readBasicAuthentication(null).isEmpty)
        // scalastyle:on null
      }
    }
  }

  describe("JWT authentication") {
    describe("Build") {
      it("should succeed if input right") {
        assert(HttpUtils.buildJwtAuthentication("token").nonEmpty)
      }
      it("should fail if argument is empty") {
        assert(HttpUtils.buildJwtAuthentication("").isEmpty)
      }
      it("should fail if argument is null") {
        // scalastyle:off null
        assert(HttpUtils.buildJwtAuthentication(null).isEmpty)
        // scalastyle:on null
      }
    }

    describe("Read") {
      it("should succeed if argument right") {
        var result: Option[String] = None
        val myToken = "token"
        for (
          token <- HttpUtils.buildJwtAuthentication(myToken);
          decoded = HttpUtils.readJwtAuthentication(token)
        ) yield {
          result = decoded
        }
        result match {
          case Some(t) => assert(t == myToken)
          case _ => fail
        }
      }
      it("should fail if argument is empty") {
        assert(HttpUtils.readJwtAuthentication("").isEmpty)
      }
      it("should fail if argument is null") {
        // scalastyle:off null
        assert(HttpUtils.readJwtAuthentication(null).isEmpty)
        // scalastyle:on null
      }
    }
  }
}
