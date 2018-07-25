package it.cwmp.services.authentication

import it.cwmp.testing.{FutureMatchers, VertxTest}
import it.cwmp.utils.Utils
import org.scalatest.{BeforeAndAfterEach, Matchers}
import scala.concurrent.Future

/**
  * Test per la classe StorageLocalDAO
  *
  * @author Davide Borficchia
  */
class StorageLocalDAOTest extends VertxTest with Matchers with FutureMatchers with BeforeAndAfterEach {

  var storageFuture: Future[AuthenticationDAO] = _
  var storageNotInizializedFuture: Future[AuthenticationDAO] = _
  var username: String = _
  var password: String = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    val storage = AuthenticationLocalDAO()
    storageFuture = storage.initialize().map(_ => storage)
    storageNotInizializedFuture = Future(storage)
    username = Utils.randomString(10)
    password = Utils.randomString(10)
  }

  describe("Storage manager") {
    describe("sign up") {
      describe("should fail with error") {
        it("when username empty") {
          val password = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signUpFuture("", password))
            .shouldFail
        }
        it("when password empty") {
          val username = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signUpFuture(username, ""))
            .shouldFail
        }
        it("when username already present") {
          val username = Utils.randomString(10)
          val password = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signUpFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.signUpFuture(username, password))
            .shouldFail
        }
      }
      describe("should succeed") {
        it("when all right") {
          val username = Utils.randomString(10)
          val password = Utils.randomString(10)
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
          val passwordWrong = Utils.randomString(10)
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
        storageNotInizializedFuture
          .flatMap(storage => storage.signUpFuture(username, password))
          .shouldFailWith[IllegalStateException]
      }
      it("sign out") {
        storageNotInizializedFuture
          .flatMap(storage => storage.signOutFuture(username))
          .shouldFailWith[IllegalStateException]
      }
      it("login") {
        storageNotInizializedFuture
          .flatMap(storage => storage.loginFuture(username, password))
          .shouldFailWith[IllegalStateException]
      }
    }
  }
}
