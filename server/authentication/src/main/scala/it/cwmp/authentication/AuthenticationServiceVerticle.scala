package it.cwmp.authentication

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.HttpServerResponse
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.utils.HttpUtils
import it.cwmp.storage.StorageAsync

import scala.concurrent.Future
import scala.util.{Failure, Success}

class AuthenticationServiceVerticle extends ScalaVerticle {

  private var storageFuture: Future[StorageAsync] = _

  override def startFuture(): Future[_] = {

    val router = Router.router(vertx)

    router.post("/api/signup").handler(handlerSignup)
    router.post("/api/signout").handler(handlerSignout)
    router.get("/api/login").handler(handlerLogin)
    router.get("/api/validate").handler(handlerValidation)

    storageFuture = vertx.fileSystem.readFileFuture("service/jdbc_config.json")
      .map(config => JDBCClient.createShared(vertx, new JsonObject(config)))
      .map(client => StorageAsync(client))
      .flatMap(storage => storage.init().map(_ => storage))

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

  private def handlerSignout(routingContext: RoutingContext): Unit = {
    val response: HttpServerResponse = routingContext.response()
    for (
      authorizationHeader <- routingContext.request().headers().get(HttpHeaderNames.AUTHORIZATION.toString);
      token <- HttpUtils.readJwtAuthentication(authorizationHeader);
      username <- JwtUtils.decodeUsernameToken(token)
    ) yield {
      // If every check pass, username contains the username contained in the token and we can check it exists
      storageFuture.map(_.signoutFuture(username).onComplete {
        case Success(_) => response setStatusCode 202 end
        case Failure(_) => sendError(401, response)
      })
      return
    }
    sendError(400, response)
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