import io.netty.handler.codec.http.HttpHeaders
import io.vertx.scala.core.http.{HttpClientOptions, HttpClientResponse}
import org.scalatest.Matchers

import scala.concurrent.Promise

class AuthenticationVerticleSpec extends VerticleTesting[AuthenticationVerticle] with Matchers {

  val client = vertx.createHttpClient(HttpClientOptions()
    .setDefaultHost("localhost")
    .setDefaultPort(8666)
  )

  describe("Signup") {
    it("when right should succed") {
      val promise = Promise[Int]

      client.get("/api/signup")
        .putHeader(HttpHeaders.Names.AUTHORIZATION, "Basic " + "base64key")//TODO gestire il base64
        .handler(res => {
        res.exceptionHandler(promise.failure)
        promise.success(res.statusCode())
        println(res)
      })
        .end()

      promise.future.map(res => res should equal(201))
    }

    it("when wrong should fail") {
      val promise = Promise[Int]

      client.get("/api/signup")
        .putHeader(HttpHeaders.Names.AUTHORIZATION, "Basic " + "base64key")//TODO gestire il base64
        .handler(res => {
          res.exceptionHandler(promise.failure)
          promise.success(res.statusCode())
          println(res)
      })
        .end()

      promise.future.map(res => res should equal(401))
    }
  }

  describe("Login") {
    it("when right should succed") {
      val promise = Promise[String]

      client.get("/api/login")
        .putHeader(HttpHeaders.Names.AUTHORIZATION, "Basic " + "base64key")//TODO gestire il base64
        .handler(res => {
          res.exceptionHandler(promise.failure)
          //promise.success(res.statusCode()) //TODO restituire token
          println(res)
      })
        .end()

      promise.future.map(res => res should equal("world")) // TODO not empty
    }

    it("when wrong should fail") {
      val promise = Promise[Int]

      client.get("/api/login")
        .putHeader(HttpHeaders.Names.AUTHORIZATION, "Basic " + "base64key")//TODO gestire il base64
        .handler(res => {
        res.exceptionHandler(promise.failure)
        promise.success(res.statusCode())
        println(res)
      })
        .end()

      promise.future.map(res => res should equal(401))
    }
  }

  describe("Verification") {
    it("when right should succed") {
      val promise = Promise[String]

      client.get("/api/signup")
        .putHeader(HttpHeaders.Names.AUTHORIZATION, "Basic " + "base64key")//TODO gestire il base64
        .handler(res => {
          res.exceptionHandler(promise.failure)
          //promise.success(res.statusCode())//TODO restituire token
          println(res)
      })
        .end()

      promise.future.map(res => res should equal("world")) // TODO not empty
    }

    it("when wrong should fail") {
      val promise = Promise[Int]

      client.get("/api/signup")
        .putHeader(HttpHeaders.Names.AUTHORIZATION, "Basic " + "base64key")//TODO gestire il base64
        .handler(res => {
          res.exceptionHandler(promise.failure)
          promise.success(res.statusCode())
          println(res)
      })
        .end()

      promise.future.map(res => res should equal(401))
    }
  }
}