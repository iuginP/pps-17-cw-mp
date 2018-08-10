package it.cwmp.services.wrapper

import io.netty.handler.codec.http.HttpResponseStatus.{CREATED, OK}
import io.vertx.scala.ext.web.client.WebClientOptions
import it.cwmp.services.{VertxClient, VertxInstance}
import it.cwmp.services.discovery.ServerParameters._
import it.cwmp.utils.Utils.httpStatusNameToCode

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * A trait describing the API wrapper for authentication service
  */
trait DiscoveryApiWrapper {

  /**
    * Publish the service in the remote discovery service.
    *
    * @param name the name of the service
    * @param host the host on which the service can be reached
    * @param port the port to use
    * @return a successful Future if the service has been published, a failed Future otherwise.
    */
  def publish(name: String, host: String, port: Int): Future[Unit]

  /**
    * Un-publish the service from the remote discovery service.
    *
    * @param name the name of the service
    * @param host the host on which the service can be reached
    * @param port the port to use
    * @return a successful Future if the service has been successfully un-published, a failed Future otherwise.
    */
  def unPublish(name: String, host: String, port: Int): Future[Unit]

  /**
    * This method is useful to search for the presence of a service
    * of the specified type in the remote discovery service.
    *
    * @param name the name of the service to search
    * @return a successful Future containing the couple (host, port) if a service is published, a failure otherwise.
    */
  def discover(name: String): Future[(String, Int)]
}

/**
  * Companion object
  */
object DiscoveryApiWrapper {

  val DEFAULT_HOST = "localhost"

  def apply(): DiscoveryApiWrapper =
    DiscoveryApiWrapper(DEFAULT_HOST, DEFAULT_PORT)

  def apply(host: String): DiscoveryApiWrapper =
    DiscoveryApiWrapper(host, DEFAULT_PORT)

  def apply(host: String, port: Int): DiscoveryApiWrapper =
    new DiscoveryApiWrapperImpl(WebClientOptions()
      .setDefaultHost(host)
      .setDefaultPort(port))

  /**
    * A default implementation class for Authentication API Wrapper
    */
  class DiscoveryApiWrapperImpl(override protected val clientOptions: WebClientOptions)
    extends DiscoveryApiWrapper with VertxInstance with VertxClient {

    override def publish(name: String, host: String, port: Int): Future[Unit] = {
      client.post(API_PUBLISH_SERVICE)
        .addQueryParam(PARAMETER_NAME, name)
        .addQueryParam(PARAMETER_HOST, host)
        .addQueryParam(PARAMETER_PORT, port toString)
        .sendFuture()
        .expectStatus(CREATED)
        .map(_ => ())
    }

    override def unPublish(name: String, host: String, port: Int): Future[Unit] = {
      client.delete(API_UNPUBLISH_SERVICE)
        .addQueryParam(PARAMETER_NAME, name)
        .addQueryParam(PARAMETER_HOST, host)
        .addQueryParam(PARAMETER_PORT, port toString)
        .sendFuture()
        .expectStatus(OK)
        .map(_ => ())
    }

    override def discover(name: String): Future[(String, Int)] = {
      client.get(API_DISCOVER_SERVICE)
        .addQueryParam(PARAMETER_NAME, name)
        .sendFuture()
        .expectStatus(OK)
        .map(response => response
          .bodyAsJsonObject()
          .map(body => (body.getString("host"), body.getInteger("port").toInt))
        )
        // Filtering only the actual optional mapping the empty one into failures
        .transform {
          case s@Success(Some(_)) => s
          case Success(None) => Failure(new Exception())
          case f@Failure(_) => f
        }
        .map(_.get)
    }
  }

}
