package it.cwmp.client.model

import java.net.InetAddress

import io.vertx.scala.core.Vertx
import it.cwmp.controller.client.RoomReceiverApiWrapper
import it.cwmp.model.{Address, Participant}
import it.cwmp.roomreceiver.controller.RoomReceiverServiceVerticle
import it.cwmp.utils.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ParticipantListReceiver {

  def listenForParticipantListFuture(onListReceived: List[Participant] => Unit): Future[Address] = {
    val token = Utils.randomString(20)
    Vertx.vertx().deployVerticleFuture(RoomReceiverServiceVerticle(
      token,
      participants => onListReceived(participants)))
      .map(_ => Address(s"http://${InetAddress.getLocalHost.getHostAddress}:${RoomReceiverApiWrapper.DEFAULT_PORT}"
        + RoomReceiverApiWrapper.API_RECEIVE_PARTICIPANTS_URL(token)))
  }
}
