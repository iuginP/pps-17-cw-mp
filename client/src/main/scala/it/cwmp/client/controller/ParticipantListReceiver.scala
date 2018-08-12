package it.cwmp.client.controller

import java.net.InetAddress

import it.cwmp.client.controller.ParticipantListReceiver.ADDRESS_TOKEN_LENGTH
import it.cwmp.model.{Address, Participant}
import it.cwmp.services.VertxInstance
import it.cwmp.services.roomreceiver.RoomReceiverServiceVerticle
import it.cwmp.services.roomreceiver.Service._
import it.cwmp.utils.Utils.stringToOption
import it.cwmp.utils.{Logging, Utils}

import scala.concurrent.Future
import scala.util.Success

/**
  * A trait implementing a one time server to receive a participant list to the game room
  */
trait ParticipantListReceiver extends VertxInstance with Logging {

  private var deploymentID: Option[String] = None

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
      .andThen { case Success(id) => deploymentID = id }
      .map(_ => Address(s"http://${InetAddress.getLocalHost.getHostAddress}:${verticle.port}"
        + createParticipantReceiverUrl(token)))
  }

  /**
    * A method that stops the server that would have received participants to entered room
    */
  def stopListeningForParticipants(): Unit = {
    log.info("Stopping one-time participants receiver server")
    deploymentID.foreach(vertx.undeploy)
  }
}

/**
  * Companion object
  */
object ParticipantListReceiver {
  private val ADDRESS_TOKEN_LENGTH = 20
}
