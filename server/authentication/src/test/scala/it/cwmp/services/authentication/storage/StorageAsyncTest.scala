package it.cwmp.services.authentication.storage

import io.vertx.scala.ext.jdbc.JDBCClient
import it.cwmp.testing.VertxTest
import it.cwmp.utils.Utils
import org.scalatest.{BeforeAndAfterEach, Matchers}

import scala.concurrent.Future

class StorageAsyncTest extends VertxTest with Matchers with BeforeAndAfterEach {

  var storageFuture: Future[StorageAsync] = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    storageFuture = vertx.fileSystem.readFileFuture("database/jdbc_config.json")
      .map(_.toJsonObject)
      .map(JDBCClient.createShared(vertx, _))
      .flatMap(client => {
        client.querySingleFuture("DROP SCHEMA PUBLIC CASCADE")
          .map(_ => StorageAsync(client))
      })
      .flatMap(storage => storage.init().map(_ => storage))
  }

  describe("Storage manager") {
    describe("sign up") {
      describe("should fail with error") {
        it("when username empty") {
          val password = Utils.randomString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signupFuture("", password))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when password empty") {
          val username = Utils.randomString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signupFuture(username, ""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when username already present") {
          val username = Utils.randomString(10)
          val password = Utils.randomString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signupFuture(username, password).map(_ => storage))
              .flatMap(storage => storage.signupFuture(username, password))
              .map(ex => ex shouldBe a[Exception])
          }
        }
      }
      describe("should succeed") {
        it("when all right") {
          val username = Utils.randomString(10)
          val password = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signupFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.signoutFuture(username))
            .map(_ => succeed)
        }
      }
    }

    describe("sign out") {
      describe("should fail with error") {
        it("when username empty") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signoutFuture(""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when username doesn't exists") {
          val username = Utils.randomString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signoutFuture(username))
              .map(ex => ex shouldBe a[Exception])
          }
        }
      }
      describe("should succeed") {
        it("when all right") {
          val username = Utils.randomString(10)
          val password = Utils.randomString(10)
          storageFuture
            .flatMap(storage => storage.signupFuture(username, password).map(_ => storage))
            .flatMap(storage => storage.signoutFuture(username))
            .map(_ => succeed)
        }
      }
    }

    describe("login") {
      describe("should fail with error") {
        it("when username empty") {
          val password = Utils.randomString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.loginFuture("", password))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when username doesn't exists") {
          val username = Utils.randomString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.loginFuture(username, ""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when password is wrong") {
          val username = Utils.randomString(10)
          val password = Utils.randomString(10)
          val passwordWrong = Utils.randomString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signupFuture(username, password).map(_ => storage))
              .flatMap(storage => storage.loginFuture(username, passwordWrong).map(_ => storage))
              .flatMap(storage => storage.signoutFuture(username))
              .map(ex => ex shouldBe a[Exception])
          }
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
            .map(_ => succeed)
        }
      }
    }
  }

}
