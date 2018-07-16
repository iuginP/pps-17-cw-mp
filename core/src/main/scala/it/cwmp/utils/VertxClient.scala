package it.cwmp.utils

import io.vertx.scala.ext.web.client.{HttpRequest, WebClient, WebClientOptions}

trait VertxClient {
  this: VertxInstance =>

  protected def clientOptions: WebClientOptions

  protected def client: WebClient = WebClient.create(vertx, clientOptions)
}

object VertxClient {

  import io.netty.handler.codec.http.HttpHeaderNames
  implicit class richHttpRequest[T](request: HttpRequest[T]) {

    def addAuthentication(username: String, password: String) =
      HttpUtils.buildBasicAuthentication(username, password)
        .map(request.putHeader(HttpHeaderNames.AUTHORIZATION.toString, _))
        .getOrElse(request)

    def addAuthentication(token: String) =
      HttpUtils.buildJwtAuthentication(token)
        .map(request.putHeader(HttpHeaderNames.AUTHORIZATION.toString, _))
        .getOrElse(request)
  }
}