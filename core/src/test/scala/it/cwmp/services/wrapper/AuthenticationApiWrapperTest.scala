package it.cwmp.services.wrapper

import it.cwmp.exceptions.HTTPException
import it.cwmp.services.testing.authentication.AuthenticationWebServiceTesting
import it.cwmp.utils.Utils

import scala.concurrent.Promise
import scala.util.Failure

class AuthenticationApiWrapperTest extends AuthenticationWebServiceTesting {

  private val auth = AuthenticationApiWrapper()

  override protected def singupTests(): Unit = {
    it("when right should succed") {
      val username = nextUsername
      val password = nextPassword

      auth.signUp(username, password)
        .map(res => res shouldNot be(""))
    }

    it("when username already exist should fail") {
      val username = nextUsername
      val password = nextPassword

      val promiseResult: Promise[Unit] = Promise()
      auth.signUp(username, password)
        .flatMap(_ => auth.signUp(username, password))
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == 400 => promiseResult.success(Unit)
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }
  }

  override protected def signoutTests(): Unit = {
    // TODO implementation
  }

  override protected def loginTests(): Unit = {
    it("when right should succed") {
      val username = nextUsername
      val password = nextPassword

      auth.signUp(username, password)
        .flatMap(_ => auth.login(username, password))
        .map(res => res shouldNot be(""))
    }

    it("when user does not exists should fail") {
      val username = nextUsername
      val password = nextPassword

      val promiseResult: Promise[Unit] = Promise()
      auth.login(username, password)
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == 401 => promiseResult.success(Unit)
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }

    it("when password is wrong should fail") {
      val username = nextUsername
      val password = nextPassword
      val passwordWrong = Utils.randomString(10)

      val promiseResult: Promise[Unit] = Promise()
      auth.signUp(username, password)
        .flatMap(_ => auth.login(username, passwordWrong))
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == 401 => promiseResult.success(Unit)
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }
  }

  override protected def validationTests(): Unit = {
    it("when right should succed") {
      val username = nextUsername
      val password = nextPassword

      auth.signUp(username, password)
        .flatMap(token => auth.validate(token))
        .map(user => assert(user.username == username))
    }

    it("when invalid token should fail") {
      val myToken = invalidToken

      val promiseResult: Promise[Unit] = Promise()
      auth.validate(myToken)
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == 400 => promiseResult.success(Unit)
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }

    it("when unauthorized token should fail") {
      val myToken = nextToken

      val promiseResult: Promise[Unit] = Promise()
      auth.validate(myToken)
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == 401 => promiseResult.success(Unit)
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }
  }
}
