package it.cwmp.roomreceiver.controller

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.http.HttpServer
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.model.Participant

import scala.concurrent.Future

object RoomReceiverServiceVerticle {

  def apply(url: String, receptionStrategy: Seq[Participant] => Unit): RoomReceiverServiceVerticle =
    new RoomReceiverServiceVerticle(url, receptionStrategy)
}

case class RoomReceiverServiceVerticle(token: String, receptionStrategy: Seq[Participant] => Unit) extends ScalaVerticle {

  var server: HttpServer = _

  override def startFuture(): Future[_] = {
    val router = Router.router(vertx)

    import it.cwmp.controller.client.RoomReceiverApiWrapper._
    router post API_RECEIVE_PARTICIPANTS_URL(token) handler updateRoomParticipantsHandler

    server = vertx.createHttpServer()
    server.requestHandler(router.accept _).listenFuture(DEFAULT_PORT)
  }

  private def updateRoomParticipantsHandler: Handler[RoutingContext] = implicit routingContext => {
    routingContext.request().bodyHandler(body =>
      extractIncomingParticipantListFromBody(body) match {
        case Some(participants) =>
          receptionStrategy(participants)
          routingContext.response()
            .endHandler(_ => server.close())
            .setStatusCode(201)
            .end
        case None =>
          routingContext.response() setStatusCode 400 end s"Invalid parameter: no participant list JSON in body"
      })

    def extractIncomingParticipantListFromBody(body: Buffer): Option[Seq[Participant]] = {
      try {
        var result = List[Participant]()
        val jsonArray = body.toJsonArray
        jsonArray.forEach {
          case jsonObject: JsonObject => Participant(jsonObject).foreach(participant => result = result :+ participant)
        }
        Some(result)
      } catch {
        case _: Throwable => None
      }
    }
  }
}

