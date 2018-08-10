package it.cwmp.services.discovery

import io.netty.handler.codec.http.HttpResponseStatus._
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.scala.ext.web.{Router, RoutingContext}
import io.vertx.scala.servicediscovery.types.HttpEndpoint
import io.vertx.scala.servicediscovery.{Record, ServiceDiscovery, ServiceDiscoveryOptions}
import io.vertx.servicediscovery.Status
import it.cwmp.services.VertxServer
import it.cwmp.services.discovery.Service._
import it.cwmp.utils.Logging
import it.cwmp.utils.Utils.httpStatusNameToCode

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * This class represents the verticle containing the discovery web service.
  * This verticle is composed by the web handler and the ServiceDiscovery.
  * It is a middleware used to publish, un-publish and lookup for a specific type of service.
  *
  * @param serverPort The port on which the web handler have to listen.
  */
case class DiscoveryServiceVerticle(override protected val serverPort: Int = DEFAULT_PORT) extends VertxServer with Logging {

  var discovery: ServiceDiscovery = _

  override protected def initServer: Future[_] = {
    discovery = ServiceDiscovery.create(vertx,
      ServiceDiscoveryOptions()
        .setAnnounceAddress("service-announce")
        .setName("my-name"))
    Future.successful(())
  }

  override def stopFuture(): Future[_] = {
    // Before closing the service, the discovery service is closed
    discovery.close()
    super.stopFuture()
  }

  override protected def initRouter(router: Router): Unit = {
    router post API_PUBLISH_SERVICE handler handlerPublishService
    router delete API_UN_PUBLISH_SERVICE handler handlerUnPublishService
    router get API_DISCOVER_SERVICE handler handlerDiscoverService
  }

  private def handlerPublishService: Handler[RoutingContext] = implicit routingContext => {
    log.debug("Received publish request.")
    (for (
      name <- request.getParam(PARAMETER_NAME);
      host <- request.getParam(PARAMETER_HOST);
      portString <- request.getParam(PARAMETER_PORT);
      port = Integer.parseInt(portString);
      requestRecord = HttpEndpoint.createRecord(name, host, port, "/")
    ) yield {
      //publish the service
      discovery.publishFuture(requestRecord) onComplete {
        case Success(record) =>
          // Publication successful
          log.info(s"Service ${record.getName} successfully published!")
          sendResponse(CREATED, Some(record.asJava.toJson.toString))
        case Failure(_) =>
          // Publication failed
          sendResponse(BAD_REQUEST)
      }
    }) orElse Some(sendResponse(BAD_REQUEST))
  }

  private def handlerUnPublishService: Handler[RoutingContext] = implicit routingContext => {
    log.debug("Received un-publish request.")
    (for (
      recordJson <- request.getParam(PARAMETER_REGISTRATION);
      record <- try {
        Some(Record.fromJson(new JsonObject(recordJson)))
      } catch {
        case _: Throwable => None
      }
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

  private def handlerDiscoverService: Handler[RoutingContext] = implicit routingContext => {
    log.debug("Received discover request.")
    (for (
      name <- request.getParam(PARAMETER_NAME)
    ) yield {
      //publish the service
      discovery.getRecordFuture(_.getName == name) onComplete {
        case Success(record) if record.getStatus == Status.UP =>
          // Publication successful
          log.info(s"Service $name successfully found!")
          val metadata: JsonObject = record.getLocation
          sendResponse(OK, Some(metadata.toString))
        case _ =>
          // publication failed
          sendResponse(BAD_REQUEST)
      }
    }) orElse Some(sendResponse(BAD_REQUEST))
  }
}
