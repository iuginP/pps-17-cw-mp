package it.cwmp.services.discovery

import io.netty.handler.codec.http.HttpResponseStatus._
import io.vertx.core.Handler
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.http.HttpServerRequest
import io.vertx.scala.ext.web.{Router, RoutingContext}
import io.vertx.scala.servicediscovery.types.HttpEndpoint
import io.vertx.scala.servicediscovery.{Record, ServiceDiscovery, ServiceDiscoveryOptions}
import it.cwmp.services.discovery.ServerParameters._
import it.cwmp.utils.Utils.httpStatusNameToCode
import it.cwmp.utils.{Logging, VertxServer}

import scala.concurrent.Future
import scala.util.{Failure, Success}

case class DiscoveryServiceVerticle() extends VertxServer with Logging {

  override protected val serverPort: Int = DEFAULT_PORT

  var discovery: ServiceDiscovery = _

  override protected def initServer: Future[_] = {
    discovery = ServiceDiscovery.create(vertx,
      ServiceDiscoveryOptions()
        .setAnnounceAddress("service-announce")
        .setName("my-name"))
    Future.successful(())
  }

  override def stopFuture(): Future[_] = {
    discovery.close()
    super.stopFuture()
  }

  override protected def initRouter(router: Router): Unit = {
    router post API_PUBLISH_SERVICE handler handlerPublishService
    router delete API_UNPUBLISH_SERVICE handler handlerUnPublishService
    router get API_DISCOVER_SERVICE handler handlerDiscoverService
  }

  private def handlerPublishService: Handler[RoutingContext] = implicit routingContext => {
    log.debug("Received publish request.")
    (for (
      record <- getRecordFromRequest(request)
    ) yield {
      //publish the service
      discovery.publishFuture(record) onComplete {
        case Success(s) =>
          // Publication successful
          log.info(s"Service ${s.getName} successfully published!")
          sendResponse(CREATED)
        case Failure(_) =>
          // Publication failed
          sendResponse(BAD_REQUEST)
      }
    }) orElse Some(sendResponse(BAD_REQUEST))
  }

  private def handlerUnPublishService: Handler[RoutingContext] = implicit routingContext => {
    log.debug("Received un-publish request.")
    (for (
      record <- getRecordFromRequest(request)
    ) yield {
      //publish the service
      discovery.unpublishFuture(record.getRegistration) onComplete {
        case Success(_) =>
          // Un-publication successful
          log.info(s"Service ${record.getName} successfully un-published!")
          sendResponse(OK)
        case Failure(_) =>
          // Un-publication failed
          sendResponse(BAD_REQUEST)
      }
    }) orElse Some(sendResponse(BAD_REQUEST))
  }

  private def getRecordFromRequest(request: HttpServerRequest): Option[Record] =
    for (
      name <- request.getParam(PARAMETER_NAME);
      host <- request.getParam(PARAMETER_HOST);
      portString <- request.getParam(PARAMETER_PORT);
      port = Integer.parseInt(portString);
      record = HttpEndpoint.createRecord(name, host, port, "/")
    ) yield record

  private def handlerDiscoverService: Handler[RoutingContext] = implicit routingContext => {
    log.debug("Received discover request.")
    (for (
      name <- request.getParam(PARAMETER_NAME)
    ) yield {
      //publish the service
      discovery.getRecordFuture(_.getName == name) onComplete {
        case Success(record) =>
          // Publication successful
          log.info(s"Service $name successfully found!")
          val metadata: JsonObject = record.getMetadata
          sendResponse(OK, Some(metadata toString))
        case Failure(_) =>
          // publication failed
          sendResponse(BAD_REQUEST)
      }
    }) orElse Some(sendResponse(BAD_REQUEST))
  }
}
