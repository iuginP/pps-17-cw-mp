package it.cwmp.services.authentication

import io.vertx.scala.ext.web.client.WebClientOptions
import it.cwmp.services.testing.authentication.AuthenticationWebServiceTesting
import it.cwmp.testing.{FutureMatchers, HttpMatchers}
import it.cwmp.utils.VertxClient

class AuthenticationServiceVerticleTest extends AuthenticationWebServiceTesting
  with HttpMatchers with FutureMatchers with VertxClient {

  override protected val clientOptions: WebClientOptions = WebClientOptions()
    .setDefaultHost("localhost")
    .setDefaultPort(8666)
    .setKeepAlive(false)

  override protected def singupTests(): Unit = {
    it("when right should succed") {
      val username = nextUsername
      val password = nextPassword

      client.post("/api/signup")
        .addAuthentication(username, password)
        .sendFuture()
        .shouldAnswerWith(201, _.exists(body => body.nonEmpty))
    }

    it("when empty header should fail") {
      client.post("/api/signup")
        .sendFuture()
        .shouldAnswerWith(400)
    }

    it("when invalid header should fail") {
      val token = invalidToken
      client.post("/api/signup")
        .addAuthentication(token)
        .sendFuture()
        .shouldAnswerWith(400)
    }

    it("when username already exist should fail") {
      val username = nextUsername
      val password = nextPassword

      client.post("/api/signup")
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(_ =>
          client.post("/api/signup")
            .addAuthentication(username, password)
            .sendFuture())
        .shouldAnswerWith(400)
    }
  }

  override protected def signoutTests(): Unit = {
    // TODO implement
  }

  override protected def loginTests(): Unit = {
    it("when right should succed") {
      val username = nextUsername
      val password = nextPassword

      client.post("/api/signup")
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(_ =>
          client.get("/api/login")
            .addAuthentication(username, password)
            .sendFuture())
        .shouldAnswerWith(200, _.exists(body => body.nonEmpty))
    }

    it("when empty header should fail") {
      client.get("/api/login")
        .sendFuture()
        .shouldAnswerWith(400)
    }

    it("when invalid header should fail") {
      val token = invalidToken
      client.get("/api/login")
        .addAuthentication(token)
        .sendFuture()
        .shouldAnswerWith(400)
    }

    it("when user does not exists should fail") {
      val username = nextUsername
      val password = nextPassword

      client.get("/api/login")
        .addAuthentication(username, password)
        .sendFuture()
        .shouldAnswerWith(401)
    }

    it("when password is wrong should fail") {
      val username = nextUsername
      val password = nextPassword
      val passwordWrong = nextPassword

      client.post("/api/signup")
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(_ =>
          client.get("/api/login")
            .addAuthentication(username, passwordWrong)
            .sendFuture())
        .shouldAnswerWith(401)
    }
  }

  override protected def validationTests(): Unit = {
    it("when right should succed") {
      val username = nextUsername
      val password = nextPassword

      client.post("/api/signup")
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(response =>
          client.get("/api/validate")
            .addAuthentication(response.bodyAsString().get)
            .sendFuture())
        .shouldAnswerWith(200, _.exists(body => body.nonEmpty))
    }

    it("when missing token should fail") {
      client.get("/api/validate")
        .sendFuture()
        .map(res => res statusCode() should equal(400))
    }

    it("when invalid token should fail") {
      val myToken = invalidToken

      client.get("/api/validate")
        .addAuthentication(myToken)
        .sendFuture()
        .shouldAnswerWith(400)
    }

    it("when unauthorized token should fail") {
      val myToken = nextToken

      client.get("/api/validate")
        .addAuthentication(myToken)
        .sendFuture()
        .shouldAnswerWith(401)
    }
  }
}