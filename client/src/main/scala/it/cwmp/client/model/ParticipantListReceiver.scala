package it.cwmp.client.model

import java.net.InetAddress

import it.cwmp.model.{Address, Participant}
import it.cwmp.services.roomreceiver.RoomReceiverServiceVerticle
import it.cwmp.services.roomreceiver.ServerParameters._
import it.cwmp.utils.{Utils, VertxInstance}

import scala.concurrent.Future

trait ParticipantListReceiver extends VertxInstance {

  def listenForParticipantListFuture(onListReceived: List[Participant] => Unit): Future[Address] = {
    val token = Utils.randomString(20)
    val verticle = RoomReceiverServiceVerticle(token, participants => onListReceived(participants))
    vertx.deployVerticleFuture(verticle)
      .map(_ => Address(s"http://${InetAddress.getLocalHost.getHostAddress}:${verticle.port}"
        + API_RECEIVE_PARTICIPANTS_URL(token)))
  }
}
