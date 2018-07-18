package it.cwmp.services.testing.roomreceiver

import it.cwmp.model.Participant
import it.cwmp.testing.VertxTest
import it.cwmp.utils.Utils
import org.scalatest.{BeforeAndAfterEach, Matchers}

import scala.concurrent.Promise

abstract class RoomReceiverTesting extends VertxTest with Matchers with BeforeAndAfterEach {

  protected var port: Int

  protected val rightToken: String = Utils.randomString(20)

  protected val wrongToken: String = Utils.randomString(20)

  protected val participants: List[Participant] = List(
    Participant("Nome", "Indirizzo"),
    Participant("Nome1", "Indirizzo1")
  )

  protected var participantsPromise: Promise[Seq[Participant]] = _

  override protected def beforeEach(): Unit =  {
    super.beforeEach()
    participantsPromise = Promise()
  }

}
