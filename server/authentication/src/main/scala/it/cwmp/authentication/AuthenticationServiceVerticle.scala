package it.cwmp.authentication

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.HttpServerResponse
import io.vertx.scala.ext.web.{Router, RoutingContext}
import java.util.Base64

import io.vertx.core.json.JsonObject
import io.vertx.scala.ext.jdbc.JDBCClient
import it.cwmp.storage.StorageAsync

import scala.concurrent.Future
import scala.util.{Failure, Success}

class AuthenticationServiceVerticle extends ScalaVerticle {

  private var storage: StorageAsync = _

  override def startFuture(): Future[_] = {

    val router = Router.router(vertx)

    router.post("/api/signup").handler(handlerSignup)
    router.get("/api/login").handler(handlerLogin)
    router.get("/api/validate").handler(handlerVerification)

    val client = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", "jdbc:hsqldb:mem:test?shutdown=true")
      .put("driver_class", "org.hsqldb.jdbcDriver")
      .put("max_pool_size", 30)
      .put("user", "SA")
      .put("password", ""))
    storage = StorageAsync(client)
    storage.init().flatMap(_ => {
      // Launch the server only after the DB has been initialized
      vertx
        .createHttpServer()
        .requestHandler(router.accept _)
        .listenFuture(8666)
    })
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
        storage.signupFuture(credential.get._1, credential.get._2) onComplete {
          case Success(_) => response setStatusCode 201 end "TOKEN" + credential.get._1 // TODO Token generato con utente
          case Failure(_) => sendError(400, response)
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
        storage.loginFuture(credential.get._1, credential.get._2) onComplete {
          case Success(_) => response setStatusCode 200 end "TOKEN" + credential.get._1 // TODO Token generato con utente
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