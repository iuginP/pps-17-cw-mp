package it.cwmp.controller

import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}

/**
  * A trait that represents a base class for an Api Wrapper
  *
  * @author Enrico Siboni
  */
trait ApiClient {

  /**
    * Creates a new WebClient that will do it's calls to "host" at "port"
    *
    * @param host the host to contact
    * @param port the port on which to contact
    * @return the WebClient created
    */
  def createWebClient(host: String, port: Int): WebClient = {
    WebClient.create(Vertx.vertx, WebClientOptions()
      .setDefaultHost(host)
      .setDefaultPort(port))
  }

  /**
    * Creates a new WebClient that will do it's calls to "host" at "port", with provided vertx instance
    *
    * @param host  the host to contact
    * @param port  the port on which to contact
    * @param vertx the vertx instance to use creating the WebClient
    * @return the WebClient created
    */
  def createWebClient(host: String, port: Int, vertx: Vertx): WebClient = {
    WebClient.create(vertx, WebClientOptions()
      .setDefaultHost(host)
      .setDefaultPort(port))
  }
}
