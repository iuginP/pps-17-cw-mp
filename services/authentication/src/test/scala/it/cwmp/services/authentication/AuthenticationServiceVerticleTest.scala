package it.cwmp.services.authentication

import io.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, CREATED, OK, UNAUTHORIZED}
import io.vertx.scala.ext.web.client.WebClientOptions
import it.cwmp.services.authentication.ServerParameters._
import it.cwmp.services.testing.authentication.AuthenticationWebServiceTesting
import it.cwmp.testing.{FutureMatchers, HttpMatchers}
import it.cwmp.utils.Utils.httpStatusNameToCode
import it.cwmp.utils.VertxClient

/**
  * A test class for Authentication service
  */
class AuthenticationServiceVerticleTest extends AuthenticationWebServiceTesting
  with HttpMatchers with FutureMatchers with VertxClient {

  override protected val clientOptions: WebClientOptions = WebClientOptions()
    .setDefaultHost("localhost")
    .setDefaultPort(DEFAULT_PORT)
    .setKeepAlive(false)

  override protected def singUpTests(): Unit = {
    it("when right should succeed") {
      val username = nextUsername
      val password = nextPassword

      client.post(API_SIGNUP)
        .addAuthentication(username, password)
        .sendFuture()
        .shouldAnswerWith(CREATED, _.exists(body => body.nonEmpty))
    }

    it("when empty header should fail") {
      client.post(API_SIGNUP)
        .sendFuture()
        .shouldAnswerWith(BAD_REQUEST)
    }

    it("when invalid header should fail") {
      val token = invalidToken
      client.post(API_SIGNUP)
        .addAuthentication(token)
        .sendFuture()
        .shouldAnswerWith(BAD_REQUEST)
    }

    it("when username already exist should fail") {
      val username = nextUsername
      val password = nextPassword

      client.post(API_SIGNUP)
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(_ =>
          client.post(API_SIGNUP)
            .addAuthentication(username, password)
            .sendFuture())
        .shouldAnswerWith(BAD_REQUEST)
    }
  }

  override protected def signOutTests(): Unit = {
    // TODO implement
  }

  override protected def loginTests(): Unit = {
    it("when right should succeed") {
      val username = nextUsername
      val password = nextPassword

      client.post(API_SIGNUP)
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(_ =>
          client.get(API_LOGIN)
            .addAuthentication(username, password)
            .sendFuture())
        .shouldAnswerWith(OK, _.exists(body => body.nonEmpty))
    }

    it("when empty header should fail") {
      client.get(API_LOGIN)
        .sendFuture()
        .shouldAnswerWith(BAD_REQUEST)
    }

    it("when invalid header should fail") {
      val token = invalidToken
      client.get(API_LOGIN)
        .addAuthentication(token)
        .sendFuture()
        .shouldAnswerWith(BAD_REQUEST)
    }

    it("when user does not exists should fail") {
      val username = nextUsername
      val password = nextPassword

      client.get(API_LOGIN)
        .addAuthentication(username, password)
        .sendFuture()
        .shouldAnswerWith(UNAUTHORIZED)
    }

    it("when password is wrong should fail") {
      val username = nextUsername
      val password = nextPassword
      val passwordWrong = nextPassword

      client.post(API_SIGNUP)
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(_ =>
          client.get(API_LOGIN)
            .addAuthentication(username, passwordWrong)
            .sendFuture())
        .shouldAnswerWith(UNAUTHORIZED)
    }
  }

  override protected def validationTests(): Unit = {
    it("when right should succed") {
      val username = nextUsername
      val password = nextPassword

      client.post(API_SIGNUP)
        .addAuthentication(username, password)
        .sendFuture()
        .flatMap(response =>
          client.get(API_VALIDATE)
            .addAuthentication(response.bodyAsString().get)
            .sendFuture())
        .shouldAnswerWith(OK, _.exists(body => body.nonEmpty))
    }

    it("when missing token should fail") {
      client.get(API_VALIDATE)
        .sendFuture()
        .map(res => res statusCode() should equal(BAD_REQUEST))
    }

    it("when invalid token should fail") {
      val myToken = invalidToken

      client.get(API_VALIDATE)
        .addAuthentication(myToken)
        .sendFuture()
        .shouldAnswerWith(BAD_REQUEST)
    }

    it("when unauthorized token should fail") {
      val myToken = nextToken

      client.get(API_VALIDATE)
        .addAuthentication(myToken)
        .sendFuture()
        .shouldAnswerWith(UNAUTHORIZED)
    }
  }
}