package it.cwmp.services.roomreceiver

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.model.Participant
import it.cwmp.utils.{Logging, VertxServer}

case class RoomReceiverServiceVerticle(token: String, receptionStrategy: List[Participant] => Unit) extends VertxServer with Logging {

  override protected val serverPort: Int = 0

  def port: Int = server.actualPort()

  override protected def initRouter(router: Router): Unit = {
    import it.cwmp.services.roomreceiver.ServerParameters._
    router post API_RECEIVE_PARTICIPANTS_URL(token) handler updateRoomParticipantsHandler
    log.info(s"Starting the RoomReceiver service with the token: $token ...")
  }

  private def updateRoomParticipantsHandler: Handler[RoutingContext] = implicit routingContext => {
    log.info("Receiving participant list...")
    routingContext.request().bodyHandler(body =>
      extractIncomingParticipantListFromBody(body) match {
        case Some(participants) =>
          log.info("List is valid.")
          log.debug("Applying reception strategy...")
          receptionStrategy(participants)
          response.endHandler(_ => server.close())
          sendResponse(201)
        case None =>
          log.info("Error: List is invalid.")
          sendResponse(400, "Invalid parameter: no participant list JSON in body")
      })

    def extractIncomingParticipantListFromBody(body: Buffer): Option[List[Participant]] = {
      try {
        var result = List[Participant]()
        val jsonArray = body.toJsonArray
        import Participant.Converters._
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

