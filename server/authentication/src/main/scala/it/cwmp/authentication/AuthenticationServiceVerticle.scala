package it.cwmp.authentication

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.HttpServerResponse
import io.vertx.scala.ext.web.{Router, RoutingContext}
import java.util.Base64
import scala.concurrent.Future
import scala.util.{Failure, Success}

class AuthenticationServiceVerticle extends ScalaVerticle {

  override def startFuture(): Future[_] = {

    val router = Router.router(vertx)

    router.post("/api/signup").handler(handlerSignup)
    router.get("/api/login").handler(handlerLogin)
    router.get("/api/validate").handler(handlerVerification)

    vertx
      .createHttpServer()
      .requestHandler(router.accept _)
      .listenFuture(8666)
  }

  private def sendError(statusCode: Int,  response: HttpServerResponse): Unit = {
    response.setStatusCode(statusCode).end()
  }

  private def handlerSignup(routingContext: RoutingContext): Unit = {
    val response = routingContext.response()
    val authorizationHeader = routingContext
      .request()
      .headers()
      .get(HttpHeaderNames.AUTHORIZATION.toString)

    if(authorizationHeader.isEmpty){
      sendError(400, response)
    } else {
      val credential = HttpUtils.readBasicAuthentication(authorizationHeader.get)
      if (credential.isEmpty){
        sendError(400, response)
      }else{
        val result: Future[Unit] = Future.successful(Unit) //TODO implementare chiamata in db
        result andThen {
          case Success(_) => response setStatusCode 201 end "TOKEN" // TODO Token generato con utente
          case Failure(_) => sendError(401, response)
        }
      }
    }
  }

  private def handlerLogin(routingContext: io.vertx.scala.ext.web.RoutingContext): Unit = {
    val response = routingContext.response()
    val authorizationHeader = routingContext
      .request()
      .headers()
      .get(HttpHeaderNames.AUTHORIZATION.toString)

    if(authorizationHeader.isEmpty){
      sendError(400, response)
    } else {
      val credential = HttpUtils.readBasicAuthentication(authorizationHeader.get)
      if (credential.isEmpty){
        sendError(400, response)
      }else{
        val result: Future[Unit] = Future.successful(Unit) //TODO implementare chiamata in db
        result andThen {
          case Success(_) => response setStatusCode 200 end "TOKEN" // TODO Token generato con utente
          case Failure(_) => sendError(401, response)
        }
      }
    }
  }

  private def handlerVerification(routingContext: io.vertx.scala.ext.web.RoutingContext): Unit = {
    val response: HttpServerResponse = routingContext.response()
    val authorizationHeader = routingContext
      .request()
      .headers()
      .get(HttpHeaderNames.AUTHORIZATION.toString)

    if(authorizationHeader.isEmpty){
      sendError(400, response)
    } else {

      val tokenDecoded = new String(
        Base64.getDecoder.decode(authorizationHeader.get.split(" ")(1)))
      if (tokenDecoded.nonEmpty){
        HttpUtils.readJwtAuthentication(tokenDecoded)
      }

      val result: Future[Unit] = Future.successful(Unit) //TODO implementare chiamata in db
      result andThen {
        case Success(_) => response end "TOKEN" // TODO Token generato con utente
        case Failure(_) => sendError(401, response)
      }
    }
  }
}