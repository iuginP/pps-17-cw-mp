package it.cwmp.testing.server.rooms

import it.cwmp.authentication.{HttpValidation, Validation}
import it.cwmp.controller.client.RoomReceiverApiWrapper
import it.cwmp.controller.rooms.RoomsServiceVerticle
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.{Address, Participant, User}
import it.cwmp.utils.HttpUtils
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

  protected implicit val myFirstAuthorizedUser: Participant = Participant("Enrico1", "http://127.0.1.1:8668/api/client/pFU9qOCU3kmYqwk1qqkl/room/participants")
  protected val mySecondAuthorizedUser: Participant = Participant("Enrico2", "http://127.0.1.1:8668/api/client/pFU9qOCU3kmYqwk1qqkl/room/participants")
  protected val myThirdAuthorizedUser: Participant = Participant("Enrico3", "http://127.0.1.1:8668/api/client/pFU9qOCU3kmYqwk1qqkl/room/participants")
  private val correctTokens = "CORRECT_TOKEN_1" :: "CORRECT_TOKEN_2" :: "CORRECT_TOKEN_3" :: Nil
  protected implicit val myFirstCorrectToken: String = correctTokens(0)
  protected val mySecondCorrectToken = correctTokens(1)
  protected val myThirdCorrectToken = correctTokens(2)

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
  private case class TestValidationStrategy() extends HttpValidation[User] {
    override def validate(input: String): Future[User] = input match {
      case token if token == correctTokens(0) => Future.successful(myFirstAuthorizedUser)
      case token if token == correctTokens(1) => Future.successful(mySecondAuthorizedUser)
      case token if token == correctTokens(2) => Future.successful(myThirdAuthorizedUser)
      case token if token == null || token.isEmpty => Future.failed(HTTPException(400))
      case _ => Future.failed(HTTPException(401))
    }

    /**
      *
      * @param authorizationHeader the header to verify
      * @return the future that will contain the future output
      */
    override def verifyAuthorization(authorizationHeader: String): Future[User] = HttpUtils.readJwtAuthentication(authorizationHeader) match {
      case Some(token) if token == correctTokens(0) => Future.successful(myFirstAuthorizedUser)
      case Some(token) if token == correctTokens(1) => Future.successful(mySecondAuthorizedUser)
      case Some(token) if token == correctTokens(2) => Future.successful(myThirdAuthorizedUser)
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
