package it.cwmp.services.authentication

import it.cwmp.testing.{FutureMatchers, VertxTest}
import it.cwmp.utils.Utils
import org.scalatest.{BeforeAndAfterEach, Matchers}

import scala.concurrent.Future

/**
  * A class for AuthenticationLocalDAO tests
  *
  * @author Davide Borficchia
  */
class AuthenticationLocalDAOTest extends VertxTest with Matchers with FutureMatchers with BeforeAndAfterEach {

  private var daoFuture: Future[AuthenticationDAO] = _
  private var username: String = _
  private var password: String = _

  private val USERNAME_LENGTH = 10
  private val PASSWORD_LENGTH = 15

  override def beforeEach(): Unit = {
    super.beforeEach()
    val storage = AuthenticationLocalDAO()
    daoFuture = storage.initialize().map(_ => storage)
    username = Utils.randomString(USERNAME_LENGTH)
    password = Utils.randomString(PASSWORD_LENGTH)
  }

  describe("AuthenticationLocalDAO") {
    describe("Sign up") {
      describe("should fail") {
        it("when username not provided") {
          for (dao <- daoFuture; assertion <- dao.signUpFuture("", password).shouldFail) yield assertion
        }
        it("when password empty") {
          for (dao <- daoFuture; assertion <- dao.signUpFuture(username, "").shouldFail) yield assertion
        }
        it("when username already present") {
          for (dao <- daoFuture;
               _ <- dao.signUpFuture(username, password);
               assertion <- dao.signUpFuture(username, password).shouldFail) yield assertion
        }
      }
      describe("should have success") {
        it("when parameters are ok") {
          for (dao <- daoFuture;
               _ <- dao.signUpFuture(username, password);
               assertion <- dao.signOutFuture(username).shouldSucceed) yield assertion
        }
      }
    }

    describe("Sign out") {
      describe("should fail with error") {
        it("when username is empty") {
          for (dao <- daoFuture; assertion <- dao.signOutFuture("").shouldFail) yield assertion
        }
        it("when username doesn't exists") {
          for (dao <- daoFuture; assertion <- dao.signOutFuture(username).shouldFail) yield assertion
        }
      }
      describe("should succeed") {
        it("when all parameters right") {
          for (dao <- daoFuture; _ <- dao.signUpFuture(username, password);
               assertion <- dao.signOutFuture(username).shouldSucceed) yield assertion
        }
      }
    }

    describe("Login") {
      describe("should fail with error") {
        it("when username empty") {
          for (dao <- daoFuture; assertion <- dao.loginFuture("", password).shouldFail) yield assertion
        }
        it("when username doesn't exists") {
          for (dao <- daoFuture; assertion <- dao.loginFuture(username, "").shouldFail) yield assertion
        }
        it("when password is wrong") {
          val passwordWrong = Utils.randomString(PASSWORD_LENGTH)
          for (dao <- daoFuture;
               _ <- dao.signUpFuture(username, password);
               assertion <- dao.loginFuture(username, passwordWrong).shouldFail) yield assertion
        }
      }
      describe("should succeed") {
        it("when all parameters right") {
          for (dao <- daoFuture; _ <- dao.signUpFuture(username, password);
               _ <- dao.loginFuture(username, password);
               assertion <- dao.signOutFuture(username).shouldSucceed) yield assertion
        }
      }
    }
  }

  describe("The Helper shouldn't work") {
    describe("if not initialized") {

      it("Sign up") {
        AuthenticationLocalDAO().signUpFuture(username, password).shouldFailWith[IllegalStateException]
      }
      it("Sign out") {
        AuthenticationLocalDAO().signOutFuture(username).shouldFailWith[IllegalStateException]
      }
      it("Login") {
        AuthenticationLocalDAO().loginFuture(username, password).shouldFailWith[IllegalStateException]
      }
    }
  }
}
