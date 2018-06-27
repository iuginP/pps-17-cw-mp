package it.cwmp.storage

import io.vertx.core.json.JsonObject
import org.scalatest.Matchers
import io.vertx.scala.ext.jdbc.JDBCClient
import it.cwmp.authentication.AuthenticationVerticle
import it.cwmp.utils.VerticleTesting

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

  describe("Storage manager") {
    describe("in sign up") {
      describe("should fail with error") {
        it("when username empty") {
          shouldFail(StorageAsync().signupFuture(getDefaultClient(), "", ""))
        }
        it("when password empty") {
          shouldFail(StorageAsync().signupFuture(getDefaultClient(), "", ""))
        }
        it("when username already present") {
          shouldFail(StorageAsync().signupFuture(getDefaultClient(), "aaa", "aaa")
            .map(_ => StorageAsync().signupFuture(getDefaultClient(), "aaa", "aaa"))
            .map(res => {
              StorageAsync().signoutFuture(getDefaultClient(), "aaa")
            }))
        }
      }
      describe("should succeed") {
        it("when all right") {
          StorageAsync().signupFuture(getDefaultClient(), "", "").map(res => {
            StorageAsync().signoutFuture(getDefaultClient(), "aaa")
            res should equal(Unit)
          })
        }
      }
    }
    describe("in login") {
      describe("should fail with error") {
        it("when username empty") {
          shouldFail(StorageAsync().loginFuture(getDefaultClient(), "", ""))
        }
        it("when username doesn't exists") {
          shouldFail(StorageAsync().loginFuture(getDefaultClient(), "", ""))
        }
        it("when password is wrong") {
          shouldFail(StorageAsync().signupFuture(getDefaultClient(), "aaa", "aaa")
            .map(_ => StorageAsync().loginFuture(getDefaultClient(), "aaa", "bbb"))
            .map(res => {
              StorageAsync().signoutFuture(getDefaultClient(), "aaa")
            }))
        }
      }
      describe("should succeed") {
        it("when all right") {
          StorageAsync().signupFuture(getDefaultClient(), "aaa", "aaa")
            .map(_ => StorageAsync().loginFuture(getDefaultClient(), "aaa", "aaa"))
            .map(res => {
              StorageAsync().signoutFuture(getDefaultClient(), "aaa")
              res should equal(Unit)
            })
        }
      }
    }
  }

}
