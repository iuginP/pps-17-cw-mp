package it.cwmp.authentication

import it.cwmp.utils.VerticleTesting
import org.scalatest.Matchers

import scala.concurrent.Promise

class AuthenticationServiceVerticleSpec extends VerticleTesting[AuthenticationServiceVerticle] with Matchers {

  val host = "127.0.0.1"
  val port = 8666

  describe("Signup") {
    it("when right should succed") {
      val promise = Promise[Int]

      vertx.createHttpClient()
        .getNow(port, host, "/api/signup",
          r => {
            r.exceptionHandler(promise.failure)
            promise.success(r.statusCode())
          })

      promise.future.map(res => res should equal(201))
    }

    it("when wrong should fail") {
      val promise = Promise[Int]

      vertx.createHttpClient()
        .getNow(port, host, "/api/signup",
          r => {
            r.exceptionHandler(promise.failure)
            promise.success(r.statusCode())
          })

      promise.future.map(res => res should equal(401))
    }
  }

  describe("Login") {
    it("when right should succed") {
      val promise = Promise[String]

      vertx.createHttpClient()
        .getNow(port, host, "/api/login",
          r => {
            r.exceptionHandler(promise.failure)
            r.bodyHandler(b => promise.success(b.toString))
          })

      promise.future.map(res => res should equal("world")) // TODO not empty
    }

    it("when wrong should fail") {
      val promise = Promise[Int]

      vertx.createHttpClient()
        .getNow(port, host, "/api/login",
          r => {
            r.exceptionHandler(promise.failure)
            promise.success(r.statusCode())
          })

      promise.future.map(res => res should equal(401))
    }
  }

  describe("Verification") {
    it("when right should succed") {
      val promise = Promise[String]

      vertx.createHttpClient()
        .getNow(port, host, "/api/validate",
          r => {
            r.exceptionHandler(promise.failure)
            r.bodyHandler(b => promise.success(b.toString))
          })

      promise.future.map(res => res should equal("world")) // TODO not empty
    }

    it("when wrong should fail") {
      val promise = Promise[Int]

      vertx.createHttpClient()
        .getNow(port, host, "/api/validate",
          r => {
            r.exceptionHandler(promise.failure)
            promise.success(r.statusCode())
          })

      promise.future.map(res => res should equal(401))
    }
  }

}