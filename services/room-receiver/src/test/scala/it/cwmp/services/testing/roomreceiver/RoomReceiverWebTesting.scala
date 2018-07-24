package it.cwmp.services.testing.roomreceiver

import it.cwmp.services.roomreceiver.RoomReceiverServiceVerticle
import it.cwmp.testing.VerticleBeforeAndAfterEach

/**
  * An abstract base class for testing the room receiver web service
  */
abstract class RoomReceiverWebTesting extends RoomReceiverTesting with VerticleBeforeAndAfterEach {

  override var port: Int = _

  override protected val verticlesBeforeEach: List[RoomReceiverServiceVerticle] =
    List(RoomReceiverServiceVerticle(rightToken, s => participantsPromise.success(s)))

  override def beforeEach(): Unit = {
    super.beforeEach()
    port = verticlesBeforeEach.head.port
  }
}
