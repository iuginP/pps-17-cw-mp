package it.cwmp.services.wrapper

import io.vertx.lang.scala.ScalaVerticle
import it.cwmp.exceptions.HTTPException
import it.cwmp.services.authentication.AuthenticationServiceVerticle
import it.cwmp.testing.{VerticleBeforeAndAfterEach, VertxTest}
import it.cwmp.utils.Utils
import org.scalatest.Matchers

import scala.concurrent.Promise
import scala.util.Failure

class AuthenticationApiWrapperTest extends VertxTest with VerticleBeforeAndAfterEach with Matchers {

  override protected def verticlesBeforeEach: List[ScalaVerticle] = AuthenticationServiceVerticle() :: Nil

  private val auth = AuthenticationApiWrapper()

  describe("Signup") {
    it("when right should succed") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)

      auth.signUp(username, password)
        .map(res => res shouldNot be("")) // TODO controllare il body per la presenza del token
    }

    it("when username already exist should fail") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)

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

  describe("Login") {
    it("when right should succed") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)

      auth.signUp(username, password)
        .flatMap(_ => auth.login(username, password))
        .map(res => res shouldNot be("")) // TODO controllare il body per la presenza del token
    }

    it("when user does not exists should fail") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)

      val promiseResult: Promise[Unit] = Promise()
      auth.login(username, password)
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == 401 => promiseResult.success(Unit)
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }

    it("when password is wrong should fail") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)
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

  describe("Validation") {
    it("when right should succed") {
      val username = Utils.randomString(10)
      val password = Utils.randomString(10)

      auth.signUp(username, password)
        .flatMap(token => auth.validate(token))
        .map(user => assert(user.username == username))
    }

    it("when invalid token should fail") {
      val myToken = "TOKEN"

      val promiseResult: Promise[Unit] = Promise()
      auth.validate(myToken)
        .onComplete({
          case Failure(HTTPException(statusCode, _)) if statusCode == 400 => promiseResult.success(Unit)
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }

    it("when unauthorized token should fail") {
      val myToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68"

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
