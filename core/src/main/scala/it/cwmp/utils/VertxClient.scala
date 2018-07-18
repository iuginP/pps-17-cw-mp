package it.cwmp.utils

import io.vertx.scala.ext.web.client.{HttpRequest, HttpResponse, WebClient, WebClientOptions}
import it.cwmp.exceptions.HTTPException

import scala.concurrent.Future
import scala.util.{Failure, Success}

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
  implicit protected def client: WebClient = cachedClient match {
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
  implicit protected def client(options: WebClientOptions): WebClient = options match {
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
    def addAuthentication(implicit token: String) =
      HttpUtils.buildJwtAuthentication(token)
        .map(request.putHeader(HttpHeaderNames.AUTHORIZATION.toString, _))
        .getOrElse(request)
  }

  /**
    * An implicit class to provide the <code>Future[HttpResponse]</code> with some more useful utilities.
    */
  implicit class richHttpResponse[T](response: Future[HttpResponse[T]]) {

    /**
      * Causes the future to fail if the status code if different from one of those passed
      * @param statusCode a varargs containing all the allowed status codes
      * @return the same future, but makes it fail if the status code is different from one of those passed
      */
    def expectStatus(statusCode: Int*) = {
      response.transform {
        case s @ Success(res) if statusCode.contains(res.statusCode()) => s
        case Success(res) => Failure(HTTPException(res.statusCode(), Some("Invalid response code")))
        case f @ Failure(_) => f
      }
    }

    /**
      *Maps the body of the request
      * @param strategy the strategy used to map the body
      * @tparam P the type of data returned from the mapping
      * @return a future containing the mapped body
      */
    def mapBody[P](strategy: Option[String] => Future[P]) = {
      response.flatMap(res => strategy(res.bodyAsString()))
    }
  }
}