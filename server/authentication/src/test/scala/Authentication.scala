import java.util.Base64
import io.netty.handler.codec.http.{HttpHeaderNames}
import io.vertx.scala.core.http.HttpClientOptions
import it.cwmp.authentication.HttpUtils
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
      val username = "username"
      val password = "password"

      client.post("/api/signup")
        .putHeader(HttpHeaderNames.AUTHORIZATION toString, "Basic " + HttpUtils.buildBasicAuthentication(username, password))
        .handler(res => {
        res.exceptionHandler(promise.failure)
        promise.success(res.statusCode())
      })
        .end()

      promise.future.map(res => res should equal(201))
    }

    it("when empty header should fail") {
      val promise = Promise[Int]

      client.post("/api/signup")
        .handler(res => {
          res.exceptionHandler(promise.failure)
          promise.success(res.statusCode())
      })
        .end()

      promise.future.map(res => res should equal(400))
    }

    it("when password is empty should fail") {
      val promise = Promise[Int]
      val username = "pippo"
      val password = ""

      client.post("/api/signup")
        .putHeader(HttpHeaderNames.AUTHORIZATION toString, "Basic " + HttpUtils.buildBasicAuthentication(username, password))
        .handler(res => {
          res.exceptionHandler(promise.failure)
          promise.success(res.statusCode())
        })
        .end()

      promise.future.map(res => res should equal(400))
    }

    it("when username is empty should fail") {
      val promise = Promise[Int]
      val username = ""
      val password = "password"

      client.post("/api/signup")
        .putHeader(HttpHeaderNames.AUTHORIZATION toString, "Basic " + HttpUtils.buildBasicAuthentication(username, password))
        .handler(res => {
          res.exceptionHandler(promise.failure)
          promise.success(res.statusCode())
        })
        .end()

      promise.future.map(res => res should equal(400))
    }
  }

  describe("Login") {
    it("when right should succed") {
      val promise = Promise[String]
      val username = "username"
      val password = "password"

      client.get("/api/login")
        .putHeader(HttpHeaderNames.AUTHORIZATION toString, "Basic " + HttpUtils.buildBasicAuthentication(username, password))
        .handler(res => {
          res.exceptionHandler(promise.failure)
          promise.success(res.statusMessage()) //TODO restituire token
      })
        .end()

      promise.future.map(res => res should not equal("")) // TODO not empty
    }

    it("when empty header should fail") {
      val promise = Promise[Int]

      client.get("/api/login")
        .handler(res => {
        res.exceptionHandler(promise.failure)
        promise.success(res.statusCode())
      })
        .end()

      promise.future.map(res => res should equal(400))
    }

    it("when password is empty should fail") {
      val promise = Promise[Int]
      val username = "username"
      val password = ""

      client.get("/api/login")
        .putHeader(HttpHeaderNames.AUTHORIZATION toString, "Basic " + HttpUtils.buildBasicAuthentication(username, password))
        .handler(res => {
        res.exceptionHandler(promise.failure)
        promise.success(res.statusCode())
      })
        .end()

      promise.future.map(res => res should equal(400))
    }

    it("when username is empty should fail") {
      val promise = Promise[Int]
      val username = ""
      val password = "password"

      client.get("/api/login")
        .putHeader(HttpHeaderNames.AUTHORIZATION toString, "Basic " + HttpUtils.buildBasicAuthentication(username, password))
        .handler(res => {
          res.exceptionHandler(promise.failure)
          promise.success(res.statusCode())
        })
        .end()

      promise.future.map(res => res should equal(400))
    }

  }

  describe("Verification") {
    it("when right should succed") {
      val promise = Promise[String]

      client.get("/api/validate")
        .putHeader(HttpHeaderNames.AUTHORIZATION toString, "Basic " +  Base64.getEncoder.encodeToString("Token".getBytes()))//TODO gestire il base64
        .handler(res => {
          res.exceptionHandler(promise.failure)
          promise.success(res.statusMessage())//TODO restituire token
      })
        .end()

      promise.future.map(res => res should equal("world")) // TODO not empty
    }

    it("when wrong should fail") {
      val promise = Promise[Int]

      client.get("/api/validate")
        //.putHeader(HttpHeaderNames.AUTHORIZATION toString, "Basic " + "base64key")//TODO gestire il base64
        .handler(res => {
          res.exceptionHandler(promise.failure)
          promise.success(res.statusCode())
      })
        .end()

      promise.future.map(res => res should equal(401))
    }
  }
}