package it.cwmp.services.authentication

import io.netty.handler.codec.http.HttpResponseStatus._
import io.vertx.scala.ext.web.client.WebClientOptions
import it.cwmp.services.VertxClient
import it.cwmp.services.authentication.Service._
import it.cwmp.services.testing.authentication.AuthenticationWebServiceTesting
import it.cwmp.testing.{FutureMatchers, HttpMatchers}
import it.cwmp.utils.Utils.httpStatusNameToCode

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
    it("when parameters right should succeed") {
      val username = nextUsername
      val password = nextPassword

      client.post(API_SIGN_UP)
        .addAuthentication(username, password)
        .sendFuture()
        .shouldAnswerWith(CREATED, _.exists(_.nonEmpty))
    }

    it("when empty header should fail") {
      client.post(API_SIGN_UP) sendFuture() shouldAnswerWith BAD_REQUEST
    }

    it("when invalid header should fail") {
      client.post(API_SIGN_UP) addAuthentication invalidToken sendFuture() shouldAnswerWith BAD_REQUEST
    }

    it("when username already exist should fail") {
      val username = nextUsername
      val password = nextPassword

      for (
        _ <- client.post(API_SIGN_UP) addAuthentication(username, password) sendFuture();
        apiRequest = client.post(API_SIGN_UP) addAuthentication(username, password) sendFuture();
        assertion <- apiRequest shouldAnswerWith BAD_REQUEST
      ) yield assertion
    }
  }

  override protected def signOutTests(): Unit = {
    it("when user exists, should succeed") {
      val username = nextUsername
      val password = nextPassword

      for (
        response <- client.post(API_SIGN_UP) addAuthentication(username, password) sendFuture();
        token = response.bodyAsString().get;
        apiRequest = client.delete(API_SIGN_OUT) addAuthentication token sendFuture();
        assertion <- apiRequest shouldAnswerWith ACCEPTED
      ) yield assertion
    }

    it("when succeeded, should have deleted the user") {
      val username = nextUsername
      val password = nextPassword

      for (
        response <- client.post(API_SIGN_UP) addAuthentication(username, password) sendFuture();
        token = response.bodyAsString().get;
        _ <- client.delete(API_SIGN_OUT) addAuthentication token sendFuture();
        apiRequest = client.get(API_VALIDATE) addAuthentication token sendFuture();
        assertion <- apiRequest shouldAnswerWith UNAUTHORIZED
      ) yield assertion
    }

    it("when empty header should fail") {
      client.delete(API_SIGN_OUT) sendFuture() shouldAnswerWith BAD_REQUEST
    }

    it("when invalid header should fail") {
      client.delete(API_SIGN_OUT) addAuthentication invalidToken sendFuture() shouldAnswerWith BAD_REQUEST
    }

    it("when user does not exists should fail") {
      client.delete(API_SIGN_OUT) addAuthentication nextToken sendFuture() shouldAnswerWith UNAUTHORIZED
    }
  }

  override protected def loginTests(): Unit = {
    it("when username and password right should succeed") {
      val username = nextUsername
      val password = nextPassword

      for (
        _ <- client.post(API_SIGN_UP) addAuthentication(username, password) sendFuture();
        apiRequest = client.get(API_LOGIN) addAuthentication(username, password) sendFuture();
        assertion <- apiRequest shouldAnswerWith(OK, _.exists(_.nonEmpty))
      ) yield assertion
    }

    it("when empty header should fail") {
      client.get(API_LOGIN) sendFuture() shouldAnswerWith BAD_REQUEST
    }

    it("when invalid header should fail") {
      client.get(API_LOGIN) addAuthentication invalidToken sendFuture() shouldAnswerWith BAD_REQUEST
    }

    it("when user does not exists should fail") {
      val username = nextUsername
      val password = nextPassword

      client.get(API_LOGIN) addAuthentication(username, password) sendFuture() shouldAnswerWith UNAUTHORIZED
    }

    it("when password is wrong should fail") {
      val username = nextUsername
      val password = nextPassword
      val passwordWrong = nextPassword

      for (
        _ <- client.post(API_SIGN_UP) addAuthentication(username, password) sendFuture();
        apiRequest = client.get(API_LOGIN) addAuthentication(username, passwordWrong) sendFuture();
        assertion <- apiRequest shouldAnswerWith UNAUTHORIZED
      ) yield assertion
    }
  }

  override protected def validationTests(): Unit = {
    it("when token right should succeed") {
      val username = nextUsername
      val password = nextPassword

      for (
        response <- client.post(API_SIGN_UP) addAuthentication(username, password) sendFuture();
        apiRequest = client.get(API_VALIDATE) addAuthentication response.bodyAsString().get sendFuture();
        assertion <- apiRequest shouldAnswerWith(OK, _.exists(_.nonEmpty))
      ) yield assertion
    }

    it("when missing token should fail") {
      client.get(API_VALIDATE).sendFuture() shouldAnswerWith BAD_REQUEST
    }

    it("when invalid token should fail") {
      client.get(API_VALIDATE) addAuthentication invalidToken sendFuture() shouldAnswerWith BAD_REQUEST
    }

    it("when unauthorized token should fail") {
      val myToken = nextToken
      client.get(API_VALIDATE) addAuthentication myToken sendFuture() shouldAnswerWith UNAUTHORIZED
    }
  }
}
