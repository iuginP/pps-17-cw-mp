package it.cwmp.services.wrapper

import io.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, UNAUTHORIZED}
import it.cwmp.exceptions.HTTPException
import it.cwmp.services.testing.authentication.AuthenticationWebServiceTesting
import it.cwmp.testing.FutureMatchers
import it.cwmp.utils.HttpUtils

/**
  * A test class for AuthenticationApiWrapper
  */
class AuthenticationApiWrapperTest extends AuthenticationWebServiceTesting with FutureMatchers {

  private val auth = AuthenticationApiWrapper()

  /**
    * @return a new authentication header with a token
    */
  protected def nextHeader: String = HttpUtils.buildJwtAuthentication(super.nextToken).get

  override protected def singUpTests(): Unit = {
    it("when username and password ok should succeed") {
      val username = nextUsername
      val password = nextPassword

      auth.signUp(username, password) map (_ shouldNot be(""))
    }

    it("when username already exist should fail") {
      val username = nextUsername
      val password = nextPassword

      auth.signUp(username, password)
        .flatMap(_ => auth.signUp(username, password))
        .shouldFailWith[HTTPException]((e: HTTPException) => e.statusCode == BAD_REQUEST.code())
    }
  }

  override protected def signOutTests(): Unit = {
    it("when user exists, should succeed") {
      val username = nextUsername
      val password = nextPassword

      auth.signUp(username, password)
        .flatMap(token => auth.signOut(token))
        .shouldSucceed
    }

    it("when invalid token should fail") {
      val myToken = invalidToken

      auth.signOut(myToken)
        .shouldFailWith[HTTPException]((e: HTTPException) => e.statusCode == BAD_REQUEST.code())
    }

    it("when unauthorized header should fail") {
      val myAuthToken = nextToken

      auth.signOut(myAuthToken)
        .shouldFailWith[HTTPException]((e: HTTPException) => e.statusCode == UNAUTHORIZED.code())
    }
  }

  override protected def loginTests(): Unit = {
    it("when username and password right should succeed") {
      val username = nextUsername
      val password = nextPassword

      auth.signUp(username, password)
        .flatMap(_ => auth.login(username, password))
        .map(_ shouldNot be(""))
    }

    it("when user does not exists should fail") {
      val username = nextUsername
      val password = nextPassword

      auth.login(username, password)
        .shouldFailWith[HTTPException]((e: HTTPException) => e.statusCode == UNAUTHORIZED.code())
    }

    it("when password is wrong should fail") {
      val username = nextUsername
      val password = nextPassword
      val passwordWrong = nextPassword

      auth.signUp(username, password)
        .flatMap(_ => auth.login(username, passwordWrong))
        .shouldFailWith[HTTPException]((e: HTTPException) => e.statusCode == UNAUTHORIZED.code())
    }
  }

  override protected def validationTests(): Unit = {
    it("when token right should succeed") {
      val username = nextUsername
      val password = nextPassword

      auth.signUp(username, password)
        .flatMap(token => auth.validate(HttpUtils.buildJwtAuthentication(token).get))
        .map(user => assert(user.username == username))
    }

    it("when invalid token should fail") {
      val myToken = invalidToken

      auth.validate(myToken)
        .shouldFailWith[HTTPException]((e: HTTPException) => e.statusCode == BAD_REQUEST.code())
    }

    it("when unauthorized header should fail") {
      val myAuthHeader = nextHeader

      auth.validate(myAuthHeader)
        .shouldFailWith[HTTPException]((e: HTTPException) => e.statusCode == UNAUTHORIZED.code())
    }
  }
}
