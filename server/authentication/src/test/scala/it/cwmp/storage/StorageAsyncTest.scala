package it.cwmp.storage

import io.vertx.core.json.JsonObject
import org.scalatest.Matchers
import io.vertx.scala.ext.jdbc.JDBCClient
import it.cwmp.authentication.AuthenticationServiceVerticle
import it.cwmp.testing.VerticleTesting

import scala.concurrent.Future
import scala.util.Random

class StorageAsyncTest extends VerticleTesting[AuthenticationServiceVerticle] with Matchers {

  var storageFuture: Future[StorageAsync] = _

  override def beforeAbs(): Unit = {
    storageFuture = vertx.fileSystem.readFileFuture("service/jdbc_config.json")
      .map(config => JDBCClient.createShared(vertx, new JsonObject(config)))
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
          val password = Random.nextString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signupFuture("", password))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when password empty") {
          val username = Random.nextString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signupFuture(username, ""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when username already present") {
          val username = Random.nextString(10)
          val password = Random.nextString(10)
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
          val username = Random.nextString(10)
          val password = Random.nextString(10)
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
          val username = Random.nextString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.signoutFuture(username))
              .map(ex => ex shouldBe a[Exception])
          }
        }
      }
      describe("should succeed") {
        it("when all right") {
          val username = Random.nextString(10)
          val password = Random.nextString(10)
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
          val password = Random.nextString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.loginFuture("", password))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when username doesn't exists") {
          val username = Random.nextString(10)
          recoverToSucceededIf[Exception] {
            storageFuture
              .flatMap(storage => storage.loginFuture(username, ""))
              .map(ex => ex shouldBe a[Exception])
          }
        }
        it("when password is wrong") {
          val username = Random.nextString(10)
          val password = Random.nextString(10)
          val passwordWrong = Random.nextString(10)
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
          val username = Random.nextString(10)
          val password = Random.nextString(10)
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
