package it.cwmp.testing.server.rooms

import it.cwmp.authentication.Validation
import it.cwmp.controller.client.ClientCommunication
import it.cwmp.controller.rooms.RoomsServiceVerticle
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.{Address, User}
import org.scalatest.BeforeAndAfterEach

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
abstract class RoomsWebServiceTesting extends RoomsTesting with BeforeAndAfterEach {

  protected implicit val myFirstAuthorizedUser: User with Address = User("Enrico1", "address1")
  protected val mySecondAuthorizedUser: User with Address = User("Enrico2", "address2")
  protected val myThirdAuthorizedUser: User with Address = User("Enrico3", "address3")
  protected implicit val myFirstCorrectToken: String = "CORRECT_TOKEN_1"
  protected val mySecondCorrectToken = "CORRECT_TOKEN_2"
  protected val myThirdCorrectToken = "CORRECT_TOKEN_3"

  private var deploymentID: String = _

  override protected def beforeEach(): Unit =
    deploymentID = Await.result(vertx.deployVerticleFuture(RoomsServiceVerticle(TestValidationStrategy(), TestClientCommunication())), 10000.millis)

  override protected def afterEach(): Unit =
    Await.result(vertx.undeployFuture(deploymentID), 10000.millis)

  /**
    * A mocked validation strategy for user tokens during testing
    *
    * @author Enrico Siboni
    */
  private case class TestValidationStrategy() extends Validation[String, User] {
    override def validate(input: String): Future[User] = input match {
      case token if token == myFirstCorrectToken => Future.successful(myFirstAuthorizedUser)
      case token if token == mySecondCorrectToken => Future.successful(mySecondAuthorizedUser)
      case token if token == myThirdCorrectToken => Future.successful(myThirdAuthorizedUser)
      case token if token == null || token.isEmpty => Future.failed(HTTPException(400))
      case _ => Future.failed(HTTPException(401))
    }
  }

  /**
    * A mocked communication strategy vs clients to use during tests
    *
    * @author Enrico Siboni
    */
  private case class TestClientCommunication() extends ClientCommunication {
    override def sendParticipantAddresses(clientAddress: String, toSend: Seq[String]): Future[Unit] =
      Future.successful(Unit)
  }

}
