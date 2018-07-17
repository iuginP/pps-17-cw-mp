package it.cwmp.services.testing.authentication

import it.cwmp.testing.VertxTest
import it.cwmp.utils.Utils
import org.scalatest.Matchers

abstract class AuthenticationTesting extends VertxTest with Matchers {

  protected def nextUsername: String = Utils.randomString(10)

  protected def nextPassword: String = Utils.randomString(10)

  private val tokens = Iterator.continually(List(
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68"
  )).flatten

  protected def nextToken: String = tokens.next()

  protected val invalidToken: String = "INVALID"


  protected def singupTests()

  describe("Sign up") {
    singupTests()
  }

  protected def signoutTests()

  describe("Sign out") {
    signoutTests()
  }

  protected def loginTests()

  describe("Login") {
    loginTests()
  }

  protected def validationTests()

  describe("Validation") {
    validationTests()
  }
}
