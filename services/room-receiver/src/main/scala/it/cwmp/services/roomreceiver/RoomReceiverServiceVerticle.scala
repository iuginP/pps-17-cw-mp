package it.cwmp.services.roomreceiver

import io.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, CREATED}
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.model.Participant
import it.cwmp.model.Participant.Converters._
import it.cwmp.services.roomreceiver.ServerParameters._
import it.cwmp.utils.Utils.{httpStatusNameToCode, stringToOption}
import it.cwmp.utils.{Logging, VertxServer}

/**
  * A class implementing a one-time service provided by clients to receive room infromation
  *
  * @param token             the token on which to listen for an authorized response
  * @param receptionStrategy the strategy to use when the data has been received
  */
case class RoomReceiverServiceVerticle(token: String, receptionStrategy: List[Participant] => Unit) extends VertxServer with Logging {

  override protected val serverPort: Int = 0

  def port: Int = server.actualPort()

  override protected def initRouter(router: Router): Unit = {
    router post createParticipantReceiverUrl(token) handler createParticipantReceiverHandler
    log.info(s"Starting the RoomReceiver service with the token: $token ...")
  }

  /**
    * Handles the reception of list of participants to the room
    */
  private def createParticipantReceiverHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Receiving participant list...")
    request.bodyHandler(body =>
      extractIncomingParticipantListFromBody(body) match {
        case Some(participants) =>
          log.info("List is valid.")
          log.debug("Applying reception strategy...")
          receptionStrategy(participants)
          response.endHandler(_ => server.close())
          sendResponse(CREATED)
        case None =>
          log.info("Error: List is invalid.")
          sendResponse(BAD_REQUEST, "Invalid parameter: no participant list JSON in body")
      })

    def extractIncomingParticipantListFromBody(body: Buffer): Option[List[Participant]] = {
      try {
        var result = List[Participant]()
        val jsonArray = body.toJsonArray
        jsonArray.forEach {
          case jsonObject: JsonObject => result = result :+ jsonObject.toParticipant
        }
        Some(result)
      } catch {
        case _: Throwable => None
      }
    }
  }
}

