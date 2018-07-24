package it.cwmp.services.authentication.storage

import it.cwmp.services.authentication.{StorageDAO, StorageLoaclDAO}
import it.cwmp.testing.{FutureMatchers, VertxTest}
import it.cwmp.utils.Utils
import org.scalatest.{BeforeAndAfterEach, Matchers}

import scala.concurrent.Future

class StorageLocalDAOTest extends VertxTest with Matchers with FutureMatchers with BeforeAndAfterEach {

  var storageFuture: Future[StorageDAO] = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    val storage = StorageLoaclDAO()
    storageFuture = storage.initialize().map(_ => storage)
  }

  describe("Storage manager") {
    describe("sign up") {
      describe("should fail with error") {
        it("when username empty") {
          val password = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signupFuture("", password))
            .shouldFail
        }
        it("when password empty") {
          val username = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signupFuture(username, ""))
            .shouldFail
        }
        it("when username already present") {
          val username = Utils.randomString(10)
          val password = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signupFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.signupFuture(username, password))
            .shouldFail
        }
      }
      describe("should succeed") {
        it("when all right") {
          val username = Utils.randomString(10)
          val password = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signupFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.signoutFuture(username))
            .shouldSucceed
        }
      }
    }

    describe("sign out") {
      describe("should fail with error") {
        it("when username empty") {
          storageFuture
            .flatMap(storage => storage.signoutFuture(""))
            .shouldFail
        }
        it("when username doesn't exists") {
          val username = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signoutFuture(username))
            .shouldFail
        }
      }
      describe("should succeed") {
        it("when all right") {
          val username = Utils.randomString(10)
          val password = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signupFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.signoutFuture(username))
            .shouldSucceed
        }
      }
    }

    describe("login") {
      describe("should fail with error") {
        it("when username empty") {
          val password = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.loginFuture("", password))
            .shouldFail
        }
        it("when username doesn't exists") {
          val username = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.loginFuture(username, ""))
            .shouldFail
        }
        it("when password is wrong") {
          val username = Utils.randomString(10)
          val password = Utils.randomString(10)
          val passwordWrong = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signupFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.loginFuture(username, passwordWrong).map(_ => storage))
            .flatMap(storage => storage.signoutFuture(username))
            .shouldFail
        }
      }
      describe("should succeed") {
        it("when all right") {
          val username = Utils.randomString(10)
          val password = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signupFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.loginFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.signoutFuture(username))
            .shouldSucceed
        }
      }
    }
  }

}
