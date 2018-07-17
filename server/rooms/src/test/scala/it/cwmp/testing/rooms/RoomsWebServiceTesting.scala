package it.cwmp.testing.rooms

import io.vertx.lang.scala.ScalaVerticle
import it.cwmp.authentication.HttpValidation
import it.cwmp.controller.client.RoomReceiverApiWrapper
import it.cwmp.controller.rooms.RoomsServiceVerticle
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.{Address, Participant, User}
import it.cwmp.testing.VerticleBeforeAndAfterEach
import it.cwmp.utils.HttpUtils

import scala.concurrent.Future

/**
  * Abstract base class to test RoomsService
  *
  * Provides mocked Validation strategy that accepts only provided users
  * and corresponding authorized tokens; those can be used to make acceptable API calls
  *
  * @author Enrico Siboni
  * @author Eugenio Pierfederici
  */
abstract class RoomsWebServiceTesting extends RoomsTesting with VerticleBeforeAndAfterEach {

  protected val participants: Map[String, Participant] = Map(
      "CORRECT_TOKEN_1" -> Participant("Enrico1", "http://127.0.1.1:8668/api/client/pFU9qOCU3kmYqwk1qqkl/room/participants"),
      "CORRECT_TOKEN_2" -> Participant("Enrico2", "http://127.0.1.1:8668/api/client/pFU9qOCU3kmYqwk1qqkl/room/participants"),
      "CORRECT_TOKEN_3" -> Participant("Enrico3", "http://127.0.1.1:8668/api/client/pFU9qOCU3kmYqwk1qqkl/room/participants"))
  protected implicit val defaultParticipant: Participant = participants.values.head
  protected val participantList: List[Participant] = participants.values.toList
  protected implicit val defaultToken: String = participants.keys.head
  protected val tokenList: List[String] = participants.keys.toList

  protected val notificationAddress = Address("notificationAddress")

  override protected val verticlesBeforeEach: List[ScalaVerticle] = RoomsServiceVerticle(TestValidationStrategy(), TestRoomReceiverApiWrapper()) :: Nil

  /**
    * A mocked validation strategy for user tokens during testing
    *
    * @author Enrico Siboni
    */
  private case class TestValidationStrategy() extends HttpValidation[User] {
    override def validate(input: String): Future[User] = Option(input).map(participants.get(_) match {
        case None => Future.failed(HTTPException(401))
        case Some(participant) => Future.successful(participant)
      }).getOrElse(Future.failed(HTTPException(400)))

    /**
      *
      * @param authorizationHeader the header to verify
      * @return the future that will contain the future output
      */
    override def verifyAuthorization(authorizationHeader: String): Future[User] = HttpUtils.readJwtAuthentication(authorizationHeader) match {
      case Some(token) => participants.get(token) match {
        case Some(participant) => Future.successful(participant)
        case _ => Future.failed(HTTPException(401))
      }
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
