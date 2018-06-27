package it.cwmp.storage

import org.scalatest.Matchers
import io.vertx.scala.ext.jdbc.JDBCClient
import it.cwmp.authentication.AuthenticationVerticle
import it.cwmp.utils.VerticleTesting

class StorageAsyncTest extends VerticleTesting[AuthenticationVerticle] with Matchers {

  describe("Storage manager") {
    describe("in sign up") {
      describe("should fail with error") {
        it("when username empty") {
          var client = JDBCClient.createShared(vertx, config)
          StorageAsync().signupFuture(client, "", "").map(res => res should equal(Unit))
        }
        it("when password empty") {
          var client = JDBCClient.createShared(vertx, config)
          StorageAsync().signupFuture(client, "", "").map(res => res should equal(Unit))
        }
        it("when username already present") {
          var client = JDBCClient.createShared(vertx, config)
          StorageAsync().signupFuture(client, "aaa", "aaa")
            .map(_ => StorageAsync().signupFuture(client, "aaa", "aaa"))
            .map(res => {
              StorageAsync().signoutFuture(client, "aaa")
              res should equal(Unit)
            })
        }
      }
      describe("should succeed") {
        it("when all right") {
          var client = JDBCClient.createShared(vertx, config)
          StorageAsync().signupFuture(client, "", "").map(res => {
            StorageAsync().signoutFuture(client, "aaa")
            res should equal(Unit)
          })
        }
      }
    }
    describe("in login") {
      describe("should fail with error") {
        it("when username empty") {
          var client = JDBCClient.createShared(vertx, config)
          StorageAsync().loginFuture(client, "", "").map(res => res should equal(Unit))
        }
        it("when username doesn't exists") {
          var client = JDBCClient.createShared(vertx, config)
          StorageAsync().loginFuture(client, "", "").map(res => res should equal(Unit))
        }
        it("when password is wrong") {
          var client = JDBCClient.createShared(vertx, config)
          StorageAsync().signupFuture(client, "aaa", "aaa")
            .map(f => StorageAsync().loginFuture(client, "aaa", "bbb"))
            .map(res => {
              StorageAsync().signoutFuture(client, "aaa")
              res should equal(Unit)
            })
        }
      }
      describe("should succeed") {
        it("when all right") {
          var client = JDBCClient.createShared(vertx, config)
          StorageAsync().signupFuture(client, "aaa", "aaa")
            .map(f => StorageAsync().loginFuture(client, "aaa", "aaa"))
            .map(res => {
              StorageAsync().signoutFuture(client, "aaa")
              res should equal(Unit)
            })
        }
      }
    }
  }

}
