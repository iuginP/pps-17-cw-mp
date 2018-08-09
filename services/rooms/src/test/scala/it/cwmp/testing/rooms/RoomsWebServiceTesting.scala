package it.cwmp.testing.rooms

import io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED
import io.vertx.lang.scala.ScalaVerticle
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.{Address, Participant, User}
import it.cwmp.services.Validation
import it.cwmp.services.rooms.RoomsServiceVerticle
import it.cwmp.services.wrapper.RoomReceiverApiWrapper
import it.cwmp.testing.VerticleBeforeAndAfterEach
import it.cwmp.utils.Utils.httpStatusNameToCode
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

  private val participantAddress = "http://127.0.1.1:8668/api/client/pFU9qOCU3kmYqwk1qqkl/room/participants"

  protected val participants: Map[String, Participant] = Map(
    "CORRECT_TOKEN_1" -> Participant("Enrico1", participantAddress),
    "CORRECT_TOKEN_2" -> Participant("Enrico2", participantAddress),
    "CORRECT_TOKEN_3" -> Participant("Enrico3", participantAddress))
  protected implicit val defaultParticipant: Participant = participants.values.head
  protected val participantList: List[Participant] = participants.values.toList
  protected implicit val defaultToken: String = participants.keys.head
  protected val tokenList: List[String] = participants.keys.toList

  protected val notificationAddress = Address("notificationAddress")

  override protected val verticlesBeforeEach: List[ScalaVerticle] = List(RoomsServiceVerticle(TestValidationStrategy(), TestRoomReceiverApiWrapper()))

  /**
    * A mocked validation strategy for user tokens during testing
    *
    * @author Enrico Siboni
    */
  private case class TestValidationStrategy() extends Validation[String, User] {

    /**
      *
      * @param authorizationHeader the header to verify
      * @return the future that will contain the future output
      */
    override def validate(authorizationHeader: String): Future[User] = HttpUtils.readJwtAuthentication(authorizationHeader) match {
      case Some(token) => participants.get(token) match {
        case Some(participant) => Future.successful(participant)
        case _ => Future.failed(HTTPException(UNAUTHORIZED))
      }
      case _ => Future.failed(HTTPException(UNAUTHORIZED))
    }
  }

  /**
    * A mocked communication strategy vs clients to use during tests
    *
    * @author Enrico Siboni
    */
  private case class TestRoomReceiverApiWrapper() extends RoomReceiverApiWrapper {
    override def sendParticipants(clientAddress: String, toSend: Seq[Participant]): Future[Unit] =
      Future.successful(())
  }

  /**
    * Cleans up the provided room, exiting the user with passed token
    */
  protected def cleanUpRoom(roomID: String)(implicit userToken: String): Future[_]

  /**
    * Cleans up the provided public room, exiting player with passed token
    */
  protected def cleanUpRoom(playersNumber: Int)(implicit userToken: String): Future[_]

}
