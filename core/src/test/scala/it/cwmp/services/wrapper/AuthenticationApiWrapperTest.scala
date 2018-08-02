package it.cwmp.services.wrapper

import io.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, UNAUTHORIZED}
import it.cwmp.exceptions.HTTPException
import it.cwmp.services.testing.authentication.AuthenticationWebServiceTesting
import it.cwmp.utils.HttpUtils

import scala.concurrent.Promise
import scala.util.Failure

/**
  * A test class for AuthenticationApiWrapper
  */
class AuthenticationApiWrapperTest extends AuthenticationWebServiceTesting {

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

      val promiseResult: Promise[Unit] = Promise()
      auth.signUp(username, password)
        .flatMap(_ => auth.signUp(username, password))
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == BAD_REQUEST.code() => promiseResult.success(())
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }
  }

  override protected def signOutTests(): Unit = {
    // TODO implementation
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

      val promiseResult: Promise[Unit] = Promise()
      auth.login(username, password)
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == UNAUTHORIZED.code() => promiseResult.success(())
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }

    it("when password is wrong should fail") {
      val username = nextUsername
      val password = nextPassword
      val passwordWrong = nextPassword

      val promiseResult: Promise[Unit] = Promise()
      auth.signUp(username, password)
        .flatMap(_ => auth.login(username, passwordWrong))
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == UNAUTHORIZED.code() => promiseResult.success(())
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
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

      val promiseResult: Promise[Unit] = Promise()
      auth.validate(myToken)
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == BAD_REQUEST.code() => promiseResult.success(())
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }

    it("when unauthorized header should fail") {
      val myAuthHeader = nextHeader

      val promiseResult: Promise[Unit] = Promise()
      auth.validate(myAuthHeader)
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == UNAUTHORIZED.code() => promiseResult.success(())
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }
  }
}
