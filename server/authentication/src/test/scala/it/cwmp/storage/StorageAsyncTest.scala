package it.cwmp.storage

import io.vertx.core.json.JsonObject
import org.scalatest.{Assertion, Matchers}
import io.vertx.scala.ext.jdbc.JDBCClient
import it.cwmp.authentication.AuthenticationVerticle
import it.cwmp.utils.VerticleTesting

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class StorageAsyncTest extends VerticleTesting[AuthenticationVerticle] with Matchers {

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
          val promise = Promise[Assertion]
          storageFuture.map(storage => storage.signupFuture("", "").onComplete({
            case Failure(_) => promise.success(succeed)
            case Success(_) => promise.failure(new Exception())
          }))
          promise.future
        }
        it("when password empty") {
          val promise = Promise[Assertion]
          storageFuture.map(storage => storage.signupFuture("", "").onComplete({
            case Failure(_) => promise.success(succeed)
            case Success(_) => promise.failure(new Exception())
          }))
          promise.future
        }
        it("when username already present") {
          val promise = Promise[Assertion]
          storageFuture.map(storage =>
            storage.signupFuture("aaa", "aaa").map(_ =>
              storage.signupFuture("aaa", "aaa").map(_ =>
                storage.signoutFuture("aaa").onComplete({
                  case Failure(_) => promise.success(succeed)
                  case Success(_) => promise.failure(new Exception())
                })
              )
            )
          )
          promise.future
        }
      }
      describe("should succeed") {
        it("when all right") {
          val promise = Promise[Assertion]
          storageFuture.map(storage =>
            storage.signupFuture("aaa", "aaa").map(_ =>
              storage.signoutFuture("aaa").onComplete({
                case Failure(_) => promise.failure(new Exception())
                case Success(_) => promise.success(succeed)
              })
            )
          )
          promise.future
        }
      }
    }
    describe("in login") {
      describe("should fail with error") {
        it("when username empty") {
          val promise = Promise[Assertion]
          storageFuture.map(storage => storage.loginFuture("", "").onComplete({
            case Failure(_) => promise.success(succeed)
            case Success(_) => promise.failure(new Exception())
          })
          )
          promise.future
        }
        it("when username doesn't exists") {
          val promise = Promise[Assertion]
          storageFuture.map(storage => storage.loginFuture("", "").onComplete({
            case Failure(_) => promise.success(succeed)
            case Success(_) => promise.failure(new Exception())
          })
          )
          promise.future
        }
        it("when password is wrong") {
          val promise = Promise[Assertion]
          storageFuture.map(storage =>
            storage.signupFuture("aaa", "aaa").map(_ =>
              storage.loginFuture("aaa", "bbb").map(_ =>
                storage.signoutFuture("aaa").onComplete({
                  case Failure(_) => promise.success(succeed)
                  case Success(_) => promise.failure(new Exception())
                })
              )
            )
          )
          promise.future
        }
      }
      describe("should succeed") {
        it("when all right") {
          val promise = Promise[Assertion]
          storageFuture.map(storage => storage.signupFuture("aaa", "aaa").map(_ =>
              storage.loginFuture("aaa", "aaa").map(_ =>
                storage.signoutFuture("aaa").onComplete({
                  case Failure(_) => promise.failure(new Exception())
                  case Success(_) => promise.success(succeed)
                })
              )
            )
          )
          promise.future
        }
      }
    }
  }

}
