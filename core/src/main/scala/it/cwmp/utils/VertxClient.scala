package it.cwmp.utils

import io.vertx.scala.ext.web.client.{HttpRequest, WebClient, WebClientOptions}

/**
  * This trait provides all the utilities to get an instance of a client in the Vertx environment.
  *
  * @author Eugenio Pierfederici
  */
trait VertxClient {
  this: VertxInstance =>

  private var cachedClient: WebClient = _

  /**
    *This method must be implemented in order to provide a default configuration for the client.
    * @return The default client configuration
    */
  protected def clientOptions: WebClientOptions

  /**
    * This method should be called to obtain an instance of the default [[WebClient]].
    * The client is cached, so it will be instantiated only the first time it is called.
    * @return the client
    */
  protected def client: WebClient = cachedClient match {
    case client: WebClient => client
    case _ =>
      cachedClient = WebClient.create(vertx, clientOptions)
      cachedClient
  }

  /**
    * When called it returns the client corresponding to the configuration passed,
    * or the default client if the configuration is null or invalid.
    * @param options the configuration to use
    * @return the client
    */
  protected def client(options: WebClientOptions): WebClient = options match {
    case options: WebClientOptions if options == clientOptions => client
    case options: WebClientOptions => WebClient.create(vertx, options)
    case _ => client
  }

  /**
    * An implicit class to provide the [[HttpRequest]] with some more useful utilities.
    */
  import io.netty.handler.codec.http.HttpHeaderNames
  implicit class richHttpRequest[T](request: HttpRequest[T]) {

    /**
      * Simplified way to add the basic Authorization header with the provided username and password
      * @param username the username
      * @param password the password
      * @return the same [[HttpRequest]] enriched, with the authorization header
      */
    def addAuthentication(username: String, password: String) =
      HttpUtils.buildBasicAuthentication(username, password)
        .map(request.putHeader(HttpHeaderNames.AUTHORIZATION.toString, _))
        .getOrElse(request)

    /**
      * Simplified way to add the jwt Authorization header with the provided jwt token
      * @param token the token
      * @return the same [[HttpRequest]] enriched, with the authorization header
      */
    def addAuthentication(token: String) =
      HttpUtils.buildJwtAuthentication(token)
        .map(request.putHeader(HttpHeaderNames.AUTHORIZATION.toString, _))
        .getOrElse(request)
  }
}