package it.cwmp.utils

import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}

trait VertxClient {
  this: VertxInstance =>

  protected def clientOptions: WebClientOptions

  protected def client: WebClient = WebClient.create(vertx, clientOptions)
}
