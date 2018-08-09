package it.cwmp.services

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.{HttpServer, HttpServerRequest, HttpServerResponse}
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.User
import it.cwmp.services.VertxServer.NO_AUTH_HEADER_IN_REQUEST_ERROR
import it.cwmp.utils.Logging
import it.cwmp.utils.Utils.{httpStatusNameToCode, stringToOption}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
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

  /**
    * Initializes the server
    *
    * @return the future that completes when the server is initialized
    */
  protected def initServer: Future[_] = Future.successful(())

  /**
    * @return the server port
    */
  protected def serverPort: Int

  /**
    * Initializes the server router for requests
    *
    * @param router the router to initialize
    */
  protected def initRouter(router: Router): Unit

  /**
    * Utility method to send back responses
    *
    * @param routingContext the routing context in which to send the error
    * @param httpCode       the http code
    * @param message        the message to send back
    */
  protected def sendResponse(httpCode: Int,
                             message: Option[String] = None)
                            (implicit routingContext: RoutingContext): Unit = {
    response.setStatusCode(httpCode)
    message match {
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
  protected def getRequestParameter(paramName: String)
                                   (implicit routingContext: RoutingContext): Option[String] =
    request.getParam(paramName)

  /**
    * Utility method to obtain the request object
    *
    * @param routingContext the implicit routing context
    * @return the request
    */
  protected def request(implicit routingContext: RoutingContext): HttpServerRequest = routingContext.request()

  /**
    * An implicit class to provide the [[HttpServerRequest]] with some more useful utilities.
    */
  implicit class RichHttpRequest(request: HttpServerRequest) {

    /**
      * This is a utility method that checks if the request is authenticated.
      * For the check it uses the validation strategy given.
      *
      * @param strategy       the validation strategy to use.
      * @param routingContext the routing context in which to execute the check.
      * @return A future containing the authenticated user, if present, otherwise it fails with a [[HTTPException]]
      */
    def checkAuthentication(implicit strategy: Validation[String, User],
                            routingContext: RoutingContext): Future[User] = getAuthenticationHeader match {
      case None =>
        log.warn(NO_AUTH_HEADER_IN_REQUEST_ERROR)
        Future.failed(HTTPException(BAD_REQUEST, NO_AUTH_HEADER_IN_REQUEST_ERROR))
      case Some(authenticationHeader) => strategy.validate(authenticationHeader)
    }

    /**
      * This is a utility method that checks if the request is authenticated.
      * For the check it uses the validation strategy given.
      * If it fails with any [[HTTPException]], then it responds to the request
      * with the status code and message specified in the exception.
      *
      * @param strategy       the validation strategy to use.
      * @param routingContext the routing context in which to execute the check.
      * @return A future containing the authenticated user, if present
      */
    def checkAuthenticationOrReject(implicit strategy: Validation[String, User],
                                    routingContext: RoutingContext): Future[User] =
      checkAuthentication(strategy, routingContext).recoverWith {
        case HTTPException(statusCode, errorMessage) =>
          sendResponse(statusCode, errorMessage)
          Future.failed(new IllegalAccessException(errorMessage))
      }

    /**
      * Reads the authorization header (with token) in the request.
      *
      * @return An optional containing the header, if present. Otherwise None
      */
    def getAuthenticationHeader: Option[String] = request.getHeader(HttpHeaderNames.AUTHORIZATION.toString)
  }

}

/**
  * Companion object
  */
object VertxServer {
  private val NO_AUTH_HEADER_IN_REQUEST_ERROR = "No authorization header in request!"
}
