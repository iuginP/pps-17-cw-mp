package it.cwmp.testing.server.rooms

import it.cwmp.authentication.Validation
import it.cwmp.controller.rooms.RoomsServiceVerticle
import it.cwmp.model.{Address, User}
import it.cwmp.testing.VertxTest
import javax.xml.ws.http.HTTPException
import org.scalatest.{BeforeAndAfterEach, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Abstract base class to test RoomsService
  *
  * Provides mocked Validation strategy that accepts only provided users
  * and corresponding authorized tokens; those can be used to make acceptable API calls
  *
  * @author Enrico Siboni
  */
abstract class RoomsServiceWebTesting extends VertxTest with Matchers with BeforeAndAfterEach {

  protected implicit val myFirstAuthorizedUser: User with Address = User("Enrico1", "address1")
  protected val mySecondAuthorizedUser: User with Address = User("Enrico2", "address2")
  protected val myThirdAuthorizedUser: User with Address = User("Enrico3", "address3")
  protected implicit val myFirstCorrectToken: String = "CORRECT_TOKEN_1"
  protected val mySecondCorrectToken = "CORRECT_TOKEN_2"
  protected val myThirdCorrectToken = "CORRECT_TOKEN_3"

  private var deploymentID: String = _

  override protected def beforeEach(): Unit =
    deploymentID = Await.result(vertx.deployVerticleFuture(RoomsServiceVerticle(TestValidationStrategy())), 10000.millis)

  override protected def afterEach(): Unit =
    Await.result(vertx.undeployFuture(deploymentID), 10000.millis)

  /**
    * A validation strategy for testing
    *
    * @author Enrico Siboni
    */
  private case class TestValidationStrategy() extends Validation[String, User] {
    override def validate(input: String): Future[User] = input match {
      case token if token == myFirstCorrectToken => Future.successful(myFirstAuthorizedUser)
      case token if token == mySecondCorrectToken => Future.successful(mySecondAuthorizedUser)
      case token if token == myThirdCorrectToken => Future.successful(myThirdAuthorizedUser)
      case token if token == null || token.isEmpty => Future.failed(new HTTPException(400))
      case _ => Future.failed(new HTTPException(401))
    }
  }

}
