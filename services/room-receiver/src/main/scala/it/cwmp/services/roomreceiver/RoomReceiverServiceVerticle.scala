package it.cwmp.services.roomreceiver

import com.typesafe.scalalogging.Logger
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.http.HttpServer
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.model.Participant

import scala.concurrent.Future

case class RoomReceiverServiceVerticle(token: String, receptionStrategy: List[Participant] => Unit) extends ScalaVerticle {

  private val logger: Logger = Logger[RoomReceiverServiceVerticle]
  private var server: HttpServer = _

  def port: Int = server.actualPort()

  override def startFuture(): Future[_] = {
    val router = Router.router(vertx)

    import it.cwmp.services.roomreceiver.ServerParameters._
    router post API_RECEIVE_PARTICIPANTS_URL(token) handler updateRoomParticipantsHandler

    logger.info(s"Starting the RoomReceiver service with the token: $token.")
    server = vertx.createHttpServer()
    server.requestHandler(router.accept _).listenFuture(0)
  }

  private def updateRoomParticipantsHandler: Handler[RoutingContext] = implicit routingContext => {
    logger.info("Receiving participant list...")
    routingContext.request().bodyHandler(body =>
      extractIncomingParticipantListFromBody(body) match {
        case Some(participants) =>
          logger.info("List is valid.")
          logger.debug("Applying reception strategy...")
          receptionStrategy(participants)
          routingContext.response()
            .endHandler(_ => server.close())
            .setStatusCode(201)
            .end
        case None =>
          logger.info("Error: List is invalid.")
          routingContext.response() setStatusCode 400 end s"Invalid parameter: no participant list JSON in body"
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
