package it.cwmp.services.testing.roomreceiver

import it.cwmp.model.Participant
import it.cwmp.services.testing.roomreceiver.RoomReceiverTesting.TOKEN_LENGTH
import it.cwmp.testing.VertxTest
import it.cwmp.utils.Utils
import org.scalatest.{BeforeAndAfterEach, Matchers}

import scala.concurrent.Promise

/**
  * An abstract base class for testing Room receiver service
  */
abstract class RoomReceiverTesting extends VertxTest with Matchers with BeforeAndAfterEach {

  protected var port: Int

  protected val rightToken: String = Utils.randomString(TOKEN_LENGTH)

  protected val wrongToken: String = Utils.randomString(TOKEN_LENGTH)

  protected val participants: List[Participant] = List(
    Participant("FirstName", "FirstAddress"),
    Participant("SecondName", "SecondAddress")
  )

  protected var participantsPromise: Promise[Seq[Participant]] = _

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    participantsPromise = Promise()
  }

}

/**
  * Companion object
  */
object RoomReceiverTesting {
  private val TOKEN_LENGTH = 20
}