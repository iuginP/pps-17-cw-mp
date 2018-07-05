package it.cwmp.authentication

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.testing.VerticleTesting
import it.cwmp.utils.HttpUtils
import org.scalatest.Matchers

class AuthenticationServiceVerticleTest extends VerticleTesting[AuthenticationServiceVerticle] with Matchers {

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

    it("when invalid header should fail") {
      val token = "INVALID"
      client.post("/api/signup")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          token)
        .sendFuture()
        .map(res => res statusCode() should equal(400))
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
              case Some(secondToken) => client.post("/api/signup")
                .putHeader(
                  HttpHeaderNames.AUTHORIZATION.toString,
                  secondToken)
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
              case Some(secondToken) => client.get("/api/login")
                .putHeader(
                  HttpHeaderNames.AUTHORIZATION.toString,
                  secondToken)
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

    it("when invalid header should fail") {
      val token = "INVALID"
      client.get("/api/login")
        .putHeader(
          HttpHeaderNames.AUTHORIZATION.toString,
          token)
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when user does not exists should fail") {
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

    it("when password is wrong should fail") {
      val username = "username"
      val password = "password"
      val passwordWrong = "passwordWRONG"

      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => fail
        case Some(token) => client.post("/api/signup")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            token)
          .sendFuture()
          .flatMap(_ => HttpUtils.buildBasicAuthentication(username, passwordWrong) match {
              case None => fail
              case Some(token2) => client.get("/api/login")
                .putHeader(
                  HttpHeaderNames.AUTHORIZATION.toString,
                  token2)
                .sendFuture()
                .map(res => res statusCode() should equal(401))
            }
          )
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
            HttpUtils.buildJwtAuthentication(response.bodyAsString.get) match {
              case None => fail
              case Some(secondToken) => client.get("/api/validate")
                .putHeader(
                  HttpHeaderNames.AUTHORIZATION.toString,
                  secondToken)
                .sendFuture()
            })
          .map(res => res statusCode() should equal(200))
      }
    }

    it("when missing token should fail") {
      client.get("/api/validate")
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when invalid token should fail") {
      val myToken = "TOKEN"

      HttpUtils.buildJwtAuthentication(myToken) match {
        case None => fail
        case Some(secondToken) => client.get("/api/validate")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            secondToken)
          .sendFuture()
          .map(res => res statusCode() should equal(400))
      }
    }

    it("when unauthorized token should fail") {
      val myToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68"

      HttpUtils.buildJwtAuthentication(myToken) match {
        case None => fail
        case Some(token) => client.get("/api/validate")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            token)
          .sendFuture()
          .map(res => res statusCode() should equal(401))
      }
    }
  }
}