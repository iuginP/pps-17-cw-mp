package it.cwmp.authentication

import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.testing.VerticleTesting
import javax.xml.ws.http.HTTPException
import org.scalatest.Matchers

import scala.concurrent.Promise
import scala.util.Failure

class AuthenticationServiceTest extends VerticleTesting[AuthenticationServiceVerticle] with Matchers {

  private val auth = AuthenticationService(
    WebClient.create(Vertx.vertx(),
    WebClientOptions()
      .setDefaultHost("localhost")
      .setDefaultPort(8666)
      .setKeepAlive(false))
  )

  describe("Signup") {
    it("when right should succed") {
      val username = "username"
      val password = "password"

      auth.signUp(username, password)
        .map(res => res shouldNot be("")) // TODO controllare il body per la presenza del token
    }

    it("when username already exist should fail") {
      val username = "username"
      val password = "password"

      val promiseResult: Promise[Unit] = Promise()
      auth.signUp(username, password)
        .flatMap(_ => auth.signUp(username, password))
        .onComplete({
          case Failure(e: HTTPException) if e.getStatusCode == 400 => promiseResult.success()
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }
  }

  describe("Login") {
    it("when right should succed") {
      val username = "username"
      val password = "password"

      auth.signUp(username, password)
        .flatMap(_ => auth.login(username, password))
        .map(res => res shouldNot be("")) // TODO controllare il body per la presenza del token
    }

    it("when user does not exists should fail") {
      val username = "username"
      val password = "password"

      val promiseResult: Promise[Unit] = Promise()
      auth.login(username, password)
        .onComplete({
          case Failure(e: HTTPException) if e.getStatusCode == 401 => promiseResult.success()
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }

    it("when password is wrong should fail") {
      val username = "username"
      val password = "password"
      val passwordWrong = "passwordWRONG"

      val promiseResult: Promise[Unit] = Promise()
      auth.signUp(username, password)
        .flatMap(_ => auth.login(username, passwordWrong))
          .andThen({
            case e => println(e)
          })
        .onComplete({
          case Failure(e: HTTPException) if e.getStatusCode == 401 => promiseResult.success()
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }
  }

  describe("Validation") {
    it("when right should succed") {
      val username = "username"
      val password = "password"

      auth.signUp(username, password)
        .flatMap(token => auth.validate(token))
        .map(_ => succeed)
    }

    it("when invalid token should fail") {
      val myToken = "TOKEN"

      val promiseResult: Promise[Unit] = Promise()
      auth.validate(myToken)
        .onComplete({
          case Failure(e: HTTPException) if e.getStatusCode == 400 => promiseResult.success()
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }

    it("when unauthorized token should fail") {
      val myToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68"

      val promiseResult: Promise[Unit] = Promise()
      auth.validate(myToken)
        .onComplete({
          case Failure(e: HTTPException) if e.getStatusCode == 401 => promiseResult.success()
          case _ => promiseResult.failure(new Exception)
        })
      promiseResult.future.map(_ => succeed)
    }
  }
}
