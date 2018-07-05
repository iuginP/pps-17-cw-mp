package it.cwmp.storage

import io.vertx.core.json.JsonObject
import org.scalatest.Matchers
import io.vertx.scala.ext.jdbc.JDBCClient
import it.cwmp.authentication.AuthenticationServiceVerticle
import it.cwmp.testing.VerticleTesting

import scala.concurrent.Future

class StorageAsyncTest extends VerticleTesting[AuthenticationServiceVerticle] with Matchers {

  private def getDefaultClient(): JDBCClient = {
    val config = new JsonObject()
      .put("url", "jdbc:hsqldb:mem:test?shutdown=true")
      .put("driver_class", "org.hsqldb.jdbcDriver")
      .put("max_pool_size", 30)
      .put("user", "SA")
      .put("password", "")
    JDBCClient.createShared(vertx, config)
  }

  var storageFuture: Future[StorageAsync] = _

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    val storage = StorageAsync(getDefaultClient())
    storageFuture = storage.init().map(_ => storage)
  }

  describe("Storage manager") {
    describe("sign up") {
      describe("should fail with error") {
        it("when username empty") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signupFuture("", ""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when password empty") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signupFuture("", ""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when username already present") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signupFuture("aaa", "aaa").map(_ => storage))
              .flatMap(storage => storage.signupFuture("aaa", "aaa"))
              .map(ex => ex shouldBe a[Exception])
          }
        }
      }
      describe("should succeed") {
        it("when all right") {
          storageFuture
            .flatMap(storage => storage.signupFuture("aaa", "aaa").map(_ => storage))
            .flatMap(storage => storage.signoutFuture("aaa"))
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
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signoutFuture("aaa"))
              .map(ex => ex shouldBe a[Exception])
          }
        }
      }
      describe("should succeed") {
        it("when all right") {
          storageFuture
            .flatMap(storage => storage.signupFuture("aaa", "aaa").map(_ => storage))
            .flatMap(storage => storage.signoutFuture("aaa"))
            .map(_ => succeed)
        }
      }
    }

    describe("login") {
      describe("should fail with error") {
        it("when username empty") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.loginFuture("", "aaa"))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when username doesn't exists") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.loginFuture("aaa", ""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when password is wrong") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signupFuture("aaa", "aaa").map(_ => storage))
              .flatMap(storage => storage.loginFuture("aaa", "bbb").map(_ => storage))
              .flatMap(storage => storage.signoutFuture("aaa"))
              .map(ex => ex shouldBe a[Exception])
          }
        }
      }
      describe("should succeed") {
        it("when all right") {
          storageFuture
            .flatMap(storage => storage.signupFuture("aaa", "aaa").map(_ => storage))
            .flatMap(storage => storage.loginFuture("aaa", "aaa").map(_ => storage))
            .flatMap(storage => storage.signoutFuture("aaa"))
            .map(_ => succeed)
        }
      }
    }
  }

}
