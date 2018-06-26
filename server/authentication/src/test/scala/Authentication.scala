import org.scalatest.Matchers

import scala.concurrent.Promise

class AuthenticationVerticleSpec extends VerticleTesting[AuthenticationVerticle] with Matchers {

  val host = "127.0.0.1"
  val port = 8666

  "AuthenticationVerticle" should s"bind to $port and succed signup" in {
    val promise = Promise[Int]

    vertx.createHttpClient()
      .getNow(port, host, "/api/signup",
        r => {
          r.exceptionHandler(promise.failure)
          promise.success(r.statusCode())
        })

    promise.future.map(res => res should equal(201))
  }

  "AuthenticationVerticle" should s"bind to $port and fail signup" in {
    val promise = Promise[Int]

    vertx.createHttpClient()
      .getNow(port, host, "/api/signup",
        r => {
          r.exceptionHandler(promise.failure)
          promise.success(r.statusCode())
        })

    promise.future.map(res => res should equal(401))
  }

  "AuthenticationVerticle" should s"bind to $port and succed login" in {
    val promise = Promise[String]

    vertx.createHttpClient()
      .getNow(port, host, "/api/login",
        r => {
          r.exceptionHandler(promise.failure)
          r.bodyHandler(b => promise.success(b.toString))
        })

    promise.future.map(res => res should equal("world")) // TODO not empty
  }

  "AuthenticationVerticle" should s"bind to $port and fail login" in {
    val promise = Promise[Int]

    vertx.createHttpClient()
      .getNow(port, host, "/api/login",
        r => {
          r.exceptionHandler(promise.failure)
          promise.success(r.statusCode())
        })

    promise.future.map(res => res should equal(401))
  }

  "AuthenticationVerticle" should s"bind to $port and succed check token" in {
    val promise = Promise[String]

    vertx.createHttpClient()
      .getNow(port, host, "/api/validate",
        r => {
          r.exceptionHandler(promise.failure)
          r.bodyHandler(b => promise.success(b.toString))
        })

    promise.future.map(res => res should equal("world")) // TODO not empty
  }

  "AuthenticationVerticle" should s"bind to $port and fail check token" in {
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