package it.cwmp.authentication

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.testing.VerticleTesting
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

      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => fail
        case Some(token) => client.post("/api/signup")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            token)
          .sendFuture()
          .map(res => res statusCode() should equal(201)) // TODO controllare anche il body per la presenza del token
      }
    }

    it("when empty header should fail") {
      client.post("/api/signup")
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when password is empty should fail") {
      val username = "pippo"
      val password = ""

      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => fail
        case Some(token) => client.post("/api/signup")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            token)
          .sendFuture()
          .map(res => res statusCode() should equal(400))
      }
    }

    it("when username is empty should fail") {
      val username = ""
      val password = "password"

      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => fail
        case Some(token) => client.post("/api/signup")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            token)
          .sendFuture()
          .map(res => res statusCode() should equal(400))
      }
    }

    it("when username already exist should fail") {
      val username = "username"
      val password = "password"

      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => fail
        case Some(token) => client.post("/api/signup")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            token)
          .sendFuture()
          .flatMap(_ =>
            HttpUtils.buildBasicAuthentication(username, password) match {
              case None => fail
              case Some(token) => client.post("/api/signup")
                .putHeader(
                  HttpHeaderNames.AUTHORIZATION.toString,
                  token)
                .sendFuture()
            })
          .map(res => res statusCode() should equal(400))
      }
    }
  }

  describe("Login") {
    it("when right should succed") {
      val username = "username"
      val password = "password"

      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => fail
        case Some(token) => client.post("/api/signup")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            token)
          .sendFuture()
          .flatMap(_ =>
            HttpUtils.buildBasicAuthentication(username, password) match {
              case None => fail
              case Some(token) => client.get("/api/login")
                .putHeader(
                  HttpHeaderNames.AUTHORIZATION.toString,
                  token)
                .sendFuture()
            })
          .map(res => res statusCode() should equal(200)) // TODO controllare anche il body per la presenza del token
      }
    }

    it("when empty header should fail") {
      client.get("/api/login")
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when user not exist should fail") {
      val username = "username"
      val password = "password"

      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => fail
        case Some(token) => client.get("/api/login")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            token)
          .sendFuture()
          .map(res => res statusCode() should equal(401))
      }
    }

    it("when password is empty should fail") {
      val username = "username"
      val password = ""

      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => fail
        case Some(token) => client.get("/api/login")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            token)
          .sendFuture()
          .map(res => res statusCode() should equal(400))
      }
    }

    it("when username is empty should fail") {
      val username = ""
      val password = "password"

      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => fail
        case Some(token) => client.get("/api/login")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            token)
          .sendFuture()
          .map(res => res statusCode() should equal(400))
      }
    }
  }

  describe("Validation") {
    it("when right should succed") {
      val username = "username"
      val password = "password"
      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => fail
        case Some(token) => client.post("/api/signup")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            token)
          .sendFuture()
          .flatMap(response =>
            client.get("/api/validate")
              .putHeader(
                HttpHeaderNames.AUTHORIZATION.toString,
                HttpUtils.buildJwtAuthentication(response.bodyAsString get))
              .sendFuture())
          .map(res => res statusCode() should equal(200))
      }
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
      // Username 'tizio':
      val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68"
      client.get("/api/validate")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          HttpUtils.buildJwtAuthentication(token))
        .sendFuture()
        .map(res => res statusCode() should equal(401))
    }
  }
}