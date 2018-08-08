package it.cwmp.services.discovery

import io.netty.handler.codec.http.HttpResponseStatus._
import io.vertx.scala.ext.web.client.WebClientOptions
import it.cwmp.services.discovery.ServerParameters._
import it.cwmp.services.testing.discovery.DiscoveryWebServiceTesting
import it.cwmp.testing.{FutureMatchers, HttpMatchers}
import it.cwmp.utils.Utils.httpStatusNameToCode
import it.cwmp.utils.VertxClient

import scala.concurrent.Future

class DiscoveryServiceVerticleTest extends DiscoveryWebServiceTesting
  with HttpMatchers with FutureMatchers with VertxClient {

  override protected val clientOptions: WebClientOptions = WebClientOptions()
    .setDefaultHost("localhost")
    .setDefaultPort(DEFAULT_PORT)
    .setKeepAlive(false)

  override protected def publishTests(): Unit = {
    it("when right parameters should succeed") {
      val name = nextName
      val host = nextHost
      val port = nextPort

      client.post(API_PUBLISH_SERVICE)
        .addQueryParam(PARAMETER_NAME, name)
        .addQueryParam(PARAMETER_HOST, host)
        .addQueryParam(PARAMETER_PORT, port toString)
        .sendFuture()
        .shouldAnswerWith(CREATED, _.exists(_.nonEmpty))
    }

    it("when empty parameters should fail") {
      client.post(API_PUBLISH_SERVICE) sendFuture() shouldAnswerWith BAD_REQUEST
    }
  }

  override protected def unPublishTests(): Unit = {
    it("when right parameters should succeed") {
      val name = nextName
      val host = nextHost
      val port = nextPort

      for (
        record <- client.post(API_PUBLISH_SERVICE)
          .addQueryParam(PARAMETER_NAME, name)
          .addQueryParam(PARAMETER_HOST, host)
          .addQueryParam(PARAMETER_PORT, port toString)
          .sendFuture()
          .mapBody(body => Future.successful(body.getOrElse("")));
        apiRequest = client.delete(API_UNPUBLISH_SERVICE)
          .addQueryParam(PARAMETER_REGISTRATION, record)
          .sendFuture();
        assertion <- apiRequest shouldAnswerWith OK
      ) yield assertion
    }

    it("when empty parameters should fail") {
      client.delete(API_UNPUBLISH_SERVICE) sendFuture() shouldAnswerWith BAD_REQUEST
    }

    it("when invalid service should fail") {
      val record = "INVALID"

      client.delete(API_UNPUBLISH_SERVICE)
        .addQueryParam(PARAMETER_REGISTRATION, record)
        .sendFuture()
        .shouldAnswerWith(BAD_REQUEST)
    }
  }

  override protected def discoveryTests(): Unit = {
    it("when right name should succeed") {
      val name = nextName
      val host = nextHost
      val port = nextPort

      for (
        _ <- client.post(API_PUBLISH_SERVICE)
          .addQueryParam(PARAMETER_NAME, name)
          .addQueryParam(PARAMETER_HOST, host)
          .addQueryParam(PARAMETER_PORT, port toString)
          .sendFuture();
        apiRequest = client.get(API_DISCOVER_SERVICE)
          .addQueryParam(PARAMETER_NAME, name)
          .sendFuture();
        assertion <- apiRequest shouldAnswerWith OK
      ) yield assertion
    }

    it("when no parameters should fail") {
      client.get(API_DISCOVER_SERVICE) sendFuture() shouldAnswerWith BAD_REQUEST
    }

    it("when service doesn't exists should fail") {
      val name = nextName

      client.get(API_DISCOVER_SERVICE)
        .addQueryParam(PARAMETER_NAME, name)
        .sendFuture()
        .shouldAnswerWith(BAD_REQUEST)
    }

  }
}