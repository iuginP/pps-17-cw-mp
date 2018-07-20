package it.cwmp.utils

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.{HttpServer, HttpServerRequest, HttpServerResponse}
import io.vertx.scala.ext.web.{Router, RoutingContext}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  *
  * This is a Utility trait that simplifies the creation and management of a vertx server.
  *
  * @author Eugenio Pierfederici
  */
trait VertxServer extends ScalaVerticle {
  this: Logging =>

  protected var server: HttpServer = _

  override def startFuture(): Future[_] = {
    val router = Router.router(vertx)
    initRouter(router)
    initServer.flatMap(_ =>
      vertx.createHttpServer()
        .requestHandler(router.accept _)
        .listenFuture(serverPort))
      .andThen {
        case Success(s) =>
          log.info(s"$getClass listening on port: $serverPort")
          server = s
        case Failure(ex) => log.error(s"Cannot start service on port: $serverPort", ex)
      }
  }

  protected def initServer: Future[_] = Future.successful(())

  protected def serverPort: Int

  protected def initRouter(router: Router): Unit

  /**
    * Utility method to send back responses
    *
    * @param routingContext the routing context in wich to send the error
    * @param httpCode       the http code
    * @param message        the message to send back
    */
  protected def sendResponse(httpCode: Int,
                             message: String = null)
                            (implicit routingContext: RoutingContext): Unit = {
    response.setStatusCode(httpCode)
    Option(message) match {
      case Some(messageString) =>
        log.info(s"Sending $httpCode response to client with message: $messageString")
        response.end(messageString)
      case None =>
        log.info(s"Sending $httpCode response to client")
        response.end()
    }
  }

  /**
    * Utility method to obtain the response object
    *
    * @param routingContext the implicit routing context
    * @return the response
    */
  protected def response(implicit routingContext: RoutingContext): HttpServerResponse = routingContext.response

  /**
    * @param routingContext the routing context on which to extract
    * @return the extracted room name
    */
  protected def getRequestParameter(paramName: String)(implicit routingContext: RoutingContext): Option[String] = {
    request.getParam(paramName)
  }

  /**
    * Utility method to obtain the request object
    *
    * @param routingContext the implicit routing context
    * @return the request
    */
  protected def request(implicit routingContext: RoutingContext): HttpServerRequest = routingContext.request

  /**
    * An implicit class to provide the [[HttpServerRequest]] with some more useful utilities.
    */

  import io.netty.handler.codec.http.HttpHeaderNames

  implicit class richHttpRequest(request: HttpServerRequest) {

    def isAuthorizedFuture(implicit strategy: Validation[String, Boolean]): Future[Boolean] = getAuthentication match {
      case None => Future.successful(false)
      case Some(authentication) => strategy.validate(authentication)
    }

    def getAuthentication: Option[String] = request.getHeader(HttpHeaderNames.AUTHORIZATION.toString)
  }

}