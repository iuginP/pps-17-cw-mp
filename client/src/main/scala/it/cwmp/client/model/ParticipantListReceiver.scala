package it.cwmp.client.model

import java.net.InetAddress

import io.vertx.scala.core.Vertx
import it.cwmp.controller.client.RoomReceiverApiWrapper
import it.cwmp.model.Participant
import it.cwmp.roomreceiver.controller.RoomReceiverServiceVerticle
import it.cwmp.utils.Utils

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait ParticipantListReceiver {

  def listenForParticipantListFuture(onListReceived: List[Participant] => Unit): Future[String] = {
    val token = Utils.randomString(20)
    Vertx.vertx().deployVerticleFuture(RoomReceiverServiceVerticle(
      token,
      participants => onListReceived(participants)))
      .map(_ => s"http://${InetAddress.getLocalHost.getHostAddress}:${RoomReceiverApiWrapper.DEFAULT_PORT}"
        + RoomReceiverApiWrapper.API_RECEIVE_PARTICIPANTS_URL(token))
  }
}
