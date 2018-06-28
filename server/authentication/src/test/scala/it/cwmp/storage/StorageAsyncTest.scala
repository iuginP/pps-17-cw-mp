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

  override def beforeAbs(): Unit = {
    val storage = StorageAsync(getDefaultClient())
    storageFuture = storage.init().map(_ => storage)
  }

  describe("Storage manager") {
    describe("in sign up") {
      describe("should fail with error") {
        it("when username empty") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .map(storage => storage.signupFuture("", ""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when password empty") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .map(storage => storage.signupFuture("", ""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when username already present") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .map(storage =>
                storage.signupFuture("aaa", "aaa").map(_ =>
                  storage.signupFuture("aaa", "aaa")
                )
              )
              .map(ex => ex shouldBe a[Exception])
          }
        }
      }
      describe("should succeed") {
        it("when all right") {
          storageFuture
            .map(storage =>
              storage.signupFuture("aaa", "aaa").map(_ =>
                storage.signoutFuture("aaa").map(_ => succeed)
              )
            )
            .map(_ => succeed)
        }
      }
    }
    describe("in login") {
      describe("should fail with error") {
        it("when username empty") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .map(storage => storage.loginFuture("", ""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when username doesn't exists") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .map(storage => storage.loginFuture("", ""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when password is wrong") {
          recoverToSucceededIf[Exception] {
            storageFuture
              .map(storage =>
                storage.signupFuture("aaa", "aaa").map(_ =>
                  storage.loginFuture("aaa", "bbb").map(_ =>
                    storage.signoutFuture("aaa"))
                )
              )
              .map(ex => ex shouldBe a[Exception])
          }
        }
      }
      describe("should succeed") {
        it("when all right") {
          storageFuture
            .map(storage =>
              storage.signupFuture("aaa", "aaa").map(_ =>
                storage.loginFuture("aaa", "aaa").map(_ =>
                  storage.signoutFuture("aaa"))
              )
            )
            .map(_ => succeed)
        }
      }
    }
  }

}
