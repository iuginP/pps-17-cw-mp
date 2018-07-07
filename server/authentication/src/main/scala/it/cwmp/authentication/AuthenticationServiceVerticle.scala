package it.cwmp.authentication

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.HttpServerResponse
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.storage.StorageAsync
import it.cwmp.utils.HttpUtils

import scala.concurrent.Future
import scala.util.{Failure, Success}

class AuthenticationServiceVerticle extends ScalaVerticle {

  private var storageFuture: Future[StorageAsync] = _

  override def startFuture(): Future[_] = {

    val router = Router.router(vertx)

    router.post("/api/signup").handler(handlerSignup)
    router.get("/api/login").handler(handlerLogin)
    router.get("/api/validate").handler(handlerValidation)

    val client = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", "jdbc:hsqldb:mem:test?shutdown=true")
      .put("driver_class", "org.hsqldb.jdbcDriver")
      .put("max_pool_size", 30)
      .put("user", "SA")
      .put("password", ""))

    val storageAsync = StorageAsync(client)
    storageFuture = StorageAsync(client).init().map(_ => storageAsync)

    vertx
      .createHttpServer()
      .requestHandler(router.accept _)
      .listenFuture(8666)
  }

  private def sendError(statusCode: Int, response: HttpServerResponse): Unit = {
    response.setStatusCode(statusCode).end()
  }

  private def handlerSignup(routingContext: RoutingContext): Unit = {
    val authorizationHeader = routingContext
      .request()
      .headers()
      .get(HttpHeaderNames.AUTHORIZATION.toString)

    if (authorizationHeader.isEmpty) {
      sendError(400, routingContext.response())
    } else {
      val credential = HttpUtils.readBasicAuthentication(authorizationHeader.get)
      if (credential.isEmpty) {
        sendError(400, routingContext.response())
      } else {
        storageFuture.map(_.signupFuture(credential.get._1, credential.get._2) onComplete {
          case Success(_) =>
            JwtUtils
              .encodeUsernameToken(credential.get._1)
              .foreach(token => routingContext.response() setStatusCode 201 end token)
          case Failure(_) => sendError(400, routingContext.response())
        })
      }
    }
  }

  private def handlerLogin(routingContext: RoutingContext): Unit = {
    val response = routingContext.response()
    val authorizationHeader = routingContext
      .request()
      .headers()
      .get(HttpHeaderNames.AUTHORIZATION.toString)

    if (authorizationHeader.isEmpty) {
      sendError(400, response)
    } else {
      val credential = HttpUtils.readBasicAuthentication(authorizationHeader.get)
      if (credential.isEmpty) {
        sendError(400, response)
      } else {
        storageFuture.map(_.loginFuture(credential.get._1, credential.get._2) onComplete {
          case Success(_) =>
            JwtUtils
              .encodeUsernameToken(credential.get._1)
              .foreach(token => response setStatusCode 200 end token)
          case Failure(_) => sendError(401, response)
        })
      }
    }
  }

  private def handlerValidation(routingContext: RoutingContext): Unit = {
    for (
      authorizationHeader <- routingContext.request().headers().get(HttpHeaderNames.AUTHORIZATION.toString);
      token <- HttpUtils.readJwtAuthentication(authorizationHeader);
      username <- JwtUtils.decodeUsernameToken(token)
    ) yield {
      // If every check pass, username contains the username contained in the token and we can check it exists
      storageFuture.map(_.existsFuture(username).onComplete {
        case Success(_) => routingContext.response() end username
        case Failure(_) => sendError(401, routingContext.response())
      })
      return
    }
    sendError(400, routingContext.response())
  }
}