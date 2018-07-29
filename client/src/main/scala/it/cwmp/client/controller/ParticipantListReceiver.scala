package it.cwmp.client.controller

import java.net.InetAddress

import it.cwmp.client.controller.ParticipantListReceiver.ADDRESS_TOKEN_LENGTH
import it.cwmp.model.{Address, Participant}
import it.cwmp.services.roomreceiver.RoomReceiverServiceVerticle
import it.cwmp.services.roomreceiver.ServerParameters._
import it.cwmp.utils.{Utils, VertxInstance}

import scala.concurrent.Future

/**
  * A trait implementing a one time server to receive a participant list to the game room
  */
trait ParticipantListReceiver extends VertxInstance {

  /**
    * Listens for a list of participants
    *
    * @param onListReceived the action to execute on list received
    * @return the Future containing the address on which to contact this listener
    */
  def listenForParticipantListFuture(onListReceived: List[Participant] => Unit): Future[Address] = {
    val token = Utils.randomString(ADDRESS_TOKEN_LENGTH)
    val verticle = RoomReceiverServiceVerticle(token, participants => onListReceived(participants))
    vertx.deployVerticleFuture(verticle)
      .map(_ => Address(s"http://${InetAddress.getLocalHost.getHostAddress}:${verticle.port}"
        + API_RECEIVE_PARTICIPANTS_URL(token)))
  }
}

/**
  * Companion object
  */
object ParticipantListReceiver {
  private val ADDRESS_TOKEN_LENGTH = 20
}