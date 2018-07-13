package it.cwmp.testing.server.rooms

import it.cwmp.authentication.Validation
import it.cwmp.controller.client.RoomReceiverApiWrapper
import it.cwmp.controller.rooms.RoomsServiceVerticle
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.{Address, Participant, User}
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

  protected implicit val myFirstAuthorizedUser: Participant = Participant("Enrico1", "address1")
  protected val mySecondAuthorizedUser: Participant = Participant("Enrico2", "address2")
  protected val myThirdAuthorizedUser: Participant = Participant("Enrico3", "address3")
  protected implicit val myFirstCorrectToken: String = "CORRECT_TOKEN_1"
  protected val mySecondCorrectToken = "CORRECT_TOKEN_2"
  protected val myThirdCorrectToken = "CORRECT_TOKEN_3"

  protected val notificationAddress = Address("notificationAddress")

  private var deploymentID: String = _

  override protected def beforeEach(): Unit =
    deploymentID = Await.result(vertx.deployVerticleFuture(RoomsServiceVerticle(TestValidationStrategy(), TestRoomReceiverApiWrapper())), 10000.millis)

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
  private case class TestRoomReceiverApiWrapper() extends RoomReceiverApiWrapper {
    override def sendParticipants(clientAddress: String, toSend: Seq[Participant]): Future[Unit] =
      Future.successful(Unit)
  }

}
