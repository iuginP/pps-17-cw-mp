package it.cwmp.authentication

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.ext.web.client.WebClientOptions
import it.cwmp.testing.{VerticleBeforeAndAfterEach, VertxTest}
import it.cwmp.utils.VertxClient._
import it.cwmp.utils.{Utils, VertxClient}
import org.scalatest.Matchers

class AuthenticationServiceVerticleTest extends VertxTest with VerticleBeforeAndAfterEach with VertxClient with Matchers {

  override protected val verticlesBeforeEach: List[ScalaVerticle] = AuthenticationServiceVerticle() :: Nil
  override protected val clientOptions: WebClientOptions = WebClientOptions()
    .setDefaultHost("localhost")
    .setDefaultPort(8666)
    .setKeepAlive(false)

  describe("Signup") {
    it("when right should succed") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)

      client.post("/api/signup")
        .addAuthentication(username, password)
        .sendFuture()
        .map(res => res statusCode() should equal(201))
    }

    it("when empty header should fail") {
      client.post("/api/signup")
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when invalid header should fail") {
      val token = "INVALID"
      client.post("/api/signup")
        .addAuthentication(token)
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when username already exist should fail") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)

      client.post("/api/signup")
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(_ =>
          client.post("/api/signup")
            .addAuthentication(username, password)
            .sendFuture())
        .map(res => res statusCode() should equal(400))
    }
  }

  describe("Login") {
    it("when right should succed") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)

      client.post("/api/signup")
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(_ =>
          client.get("/api/login")
            .addAuthentication(username, password)
            .sendFuture())
        .map(res => res statusCode() should equal(200)) // TODO controllare anche il body per la presenza del token
    }

    it("when empty header should fail") {
      client.get("/api/login")
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when invalid header should fail") {
      val token = "INVALID"
      client.get("/api/login")
        .addAuthentication(token)
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when user does not exists should fail") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)

      client.get("/api/login")
        .addAuthentication(username, password)
        .sendFuture()
        .map(res => res statusCode() should equal(401))
    }

    it("when password is wrong should fail") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)
      val passwordWrong = "passwordWRONG"

      client.post("/api/signup")
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(_ =>
          client.get("/api/login")
            .addAuthentication(username, passwordWrong)
            .sendFuture())
        .map(res => res statusCode() should equal(401))
    }
  }

  describe("Validation") {
    it("when right should succed") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)

      client.post("/api/signup")
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(response =>
          client.get("/api/validate")
            .addAuthentication(response.bodyAsString().get)
            .sendFuture())
        .map(res => res statusCode() should equal(200))
    }

    it("when missing token should fail") {
      client.get("/api/validate")
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when invalid token should fail") {
      val myToken = "TOKEN"

      client.get("/api/validate")
        .addAuthentication(myToken)
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when unauthorized token should fail") {
      val myToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68"

      client.get("/api/validate")
        .addAuthentication(myToken)
        .sendFuture()
        .map(res => res statusCode() should equal(401))
    }
  }
}