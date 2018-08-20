package it.cwmp.services.wrapper

import io.vertx.core.http.HttpMethod
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.client.WebClientOptions
import it.cwmp.model.Participant
import it.cwmp.model.Participant.Converters._
import it.cwmp.services.{VertxClient, VertxInstance}
import it.cwmp.utils.AdvancedLogging

import scala.concurrent.Future

/**
  * A trait that describes the communication that the server has to have with clients
  *
  * @author Enrico Siboni
  * @author Eugenio Pierfederici
  */
trait RoomReceiverApiWrapper {

  /**
    * Sends the participants to the client
    *
    * @param toSend the participants to send
    * @return a Future that completes when the client received the data
    */
  def sendParticipants(clientAddress: String, toSend: Seq[Participant]): Future[Unit]

}

/**
  * Companion object
  */
object RoomReceiverApiWrapper {

  def apply(): RoomReceiverApiWrapper = RoomReceiverApiWrapperDefault()

  /**
    * Default implementation for client communication
    */
  private case class RoomReceiverApiWrapperDefault()
    extends RoomReceiverApiWrapper with VertxInstance with VertxClient with AdvancedLogging {

    override def sendParticipants(clientAddress: String, toSend: Seq[Participant]): Future[Unit] = {
      client
        .requestAbs(HttpMethod.POST, clientAddress)
        .sendJsonFuture(toSend.foldLeft(Json emptyArr())(_ add _.toJson))
        .logSuccessInfo(s"Sending participant to $clientAddress succeeded")
        .map(_ => Future.successful(()))
    }

    override protected def clientOptions: WebClientOptions = WebClientOptions()
  }

}
