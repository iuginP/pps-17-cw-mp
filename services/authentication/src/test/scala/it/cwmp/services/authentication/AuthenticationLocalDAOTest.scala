package it.cwmp.services.authentication

import it.cwmp.testing.{FutureMatchers, VertxTest}
import it.cwmp.utils.Utils
import org.scalatest.{BeforeAndAfterEach, Matchers}

import scala.concurrent.Future

/**
  * Test per la classe AuthenticationLocalDAO
  *
  * @author Davide Borficchia
  */
class AuthenticationLocalDAOTest extends VertxTest with Matchers with FutureMatchers with BeforeAndAfterEach {

  var storageFuture: Future[AuthenticationDAO] = _
  var storageNotInitializedFuture: Future[AuthenticationDAO] = _
  var username: String = _
  var password: String = _

  private val USERNAME_LENGTH = 10
  private val PASSWORD_LENGTH = 15

  override def beforeEach(): Unit = {
    super.beforeEach()
    val storage = AuthenticationLocalDAO()
    storageFuture = storage.initialize().map(_ => storage)
    storageNotInitializedFuture = Future(storage)
    username = Utils.randomString(USERNAME_LENGTH)
    password = Utils.randomString(PASSWORD_LENGTH)
  }

  describe("Storage manager") {
    describe("sign up") {
      describe("should fail with error") {
        it("when username empty") {
          storageFuture
            .flatMap(storage => storage.signUpFuture("", password))
            .shouldFail
        }
        it("when password empty") {
          storageFuture
            .flatMap(storage => storage.signUpFuture(username, ""))
            .shouldFail
        }
        it("when username already present") {
          storageFuture
            .flatMap(storage => storage.signUpFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.signUpFuture(username, password))
            .shouldFail
        }
      }
      describe("should succeed") {
        it("when all right") {
          storageFuture
            .flatMap(storage => storage.signUpFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.signOutFuture(username))
            .shouldSucceed
        }
      }
    }

    describe("sign out") {
      describe("should fail with error") {
        it("when username empty") {
          storageFuture
            .flatMap(storage => storage.signOutFuture(""))
            .shouldFail
        }
        it("when username doesn't exists") {
          storageFuture
            .flatMap(storage => storage.signOutFuture(username))
            .shouldFail
        }
      }
      describe("should succeed") {
        it("when all right") {
          storageFuture
            .flatMap(storage => storage.signUpFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.signOutFuture(username))
            .shouldSucceed
        }
      }
    }

    describe("login") {
      describe("should fail with error") {
        it("when username empty") {
          storageFuture
            .flatMap(storage => storage.loginFuture("", password))
            .shouldFail
        }
        it("when username doesn't exists") {
          storageFuture
            .flatMap(storage => storage.loginFuture(username, ""))
            .shouldFail
        }
        it("when password is wrong") {
          val passwordWrong = Utils.randomString(PASSWORD_LENGTH)
          storageFuture
            .flatMap(storage => storage.signUpFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.loginFuture(username, passwordWrong).map(_ => storage))
            .flatMap(storage => storage.signOutFuture(username))
            .shouldFail
        }
      }
      describe("should succeed") {
        it("when all right") {
          storageFuture
            .flatMap(storage => storage.signUpFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.loginFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.signOutFuture(username))
            .shouldSucceed
        }
      }
    }
  }
  describe("The Helper shouldn't work") {
    describe("if not initialized") {

      it("sign up") {
        storageNotInitializedFuture
          .flatMap(storage => storage.signUpFuture(username, password))
          .shouldFailWith[IllegalStateException]
      }
      it("sign out") {
        storageNotInitializedFuture
          .flatMap(storage => storage.signOutFuture(username))
          .shouldFailWith[IllegalStateException]
      }
      it("login") {
        storageNotInitializedFuture
          .flatMap(storage => storage.loginFuture(username, password))
          .shouldFailWith[IllegalStateException]
      }
    }
  }
}
