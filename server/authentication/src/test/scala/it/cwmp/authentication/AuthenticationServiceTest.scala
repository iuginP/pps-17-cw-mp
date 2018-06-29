package it.cwmp.authentication

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.utils.VerticleTesting
import org.scalatest.Matchers

class AuthenticationServiceTest extends VerticleTesting[AuthenticationServiceVerticle] with Matchers {

  private def client = {
    val options = WebClientOptions()
      .setDefaultHost("localhost")
      .setDefaultPort(8666)
      .setKeepAlive(false)
    WebClient.create(vertx, options)
  }

  describe("Signup") {
    it("when right should succed") {
      val username = "username"
      val password = "password"

      client.post("/api/signup")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildBasicAuthentication(username, password))
        .sendFuture()
        .map(res => res statusCode() should equal(201)) // TODO controllare anche il body per la presenza del token
    }

    it("when empty header should fail") {
      client.post("/api/signup")
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when password is empty should fail") {
      val username = "pippo"
      val password = ""

      client.post("/api/signup")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildBasicAuthentication(username, password))
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when username is empty should fail") {
      val username = ""
      val password = "password"

      client.post("/api/signup")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildBasicAuthentication(username, password))
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when username already exist should fail") {
      val username = "username"
      val password = "password"

      client.post("/api/signup")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildBasicAuthentication(username, password))
        .sendFuture()
        .flatMap(_ =>
          client.post("/api/signup")
            .putHeader(
              HttpHeaderNames.AUTHORIZATION.toString,
              HttpUtils.buildBasicAuthentication(username, password))
            .sendFuture())
        .map(res => res statusCode() should equal(400))
    }
  }

  describe("Login") {
    it("when right should succed") {
      val username = "username"
      val password = "password"

      client.post("/api/signup")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildBasicAuthentication(username, password))
        .sendFuture()
        .flatMap(_ =>
          client.get("/api/login")
            .putHeader(
              HttpHeaderNames.AUTHORIZATION.toString,
              HttpUtils.buildBasicAuthentication(username, password))
            .sendFuture())
        .map(res => res statusCode() should equal(200)) // TODO controllare anche il body per la presenza del token
    }

    it("when empty header should fail") {
      client.get("/api/login")
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when user not exist should fail") {
      val username = "username"
      val password = "password"

      client.get("/api/login")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildBasicAuthentication(username, password))
        .sendFuture()
        .map(res => res statusCode() should equal(401))
    }

    it("when password is empty should fail") {
      val username = "username"
      val password = ""

      client.get("/api/login")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildBasicAuthentication(username, password))
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when username is empty should fail") {
      val username = ""
      val password = "password"

      client.get("/api/login")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildBasicAuthentication(username, password))
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }
  }

  describe("Verification") {
    it("when right should succed") {
      val token = "TOKEN" // TODO ottenere token valido
      client.get("/api/validate")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildJwtAuthentication(token))
        .sendFuture()
        .map(res => res statusCode() should equal(200))
    }

    it("when missing token should fail") {
      client.get("/api/validate")
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when invalid token should fail") {
      val token = "TOKEN"
      client.get("/api/validate")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildJwtAuthentication(token))
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when unauthorized token should fail") {
      val token = "6a5sd4f6a5sd4fa6s5df4"
      client.get("/api/validate")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildJwtAuthentication(token))
        .sendFuture()
        .map(res => res statusCode() should equal(401))
    }
  }
}