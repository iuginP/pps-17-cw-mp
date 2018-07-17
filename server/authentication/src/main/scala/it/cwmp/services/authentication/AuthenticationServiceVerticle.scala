package it.cwmp.services.authentication

import com.typesafe.scalalogging.Logger
import io.vertx.scala.core.http.HttpServerResponse
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.storage.StorageAsync
import it.cwmp.utils.{HttpUtils, VertxServer}

import scala.concurrent.Future
import scala.util.{Failure, Success}

case class AuthenticationServiceVerticle() extends VertxServer {

  private val logger: Logger = Logger[AuthenticationServiceVerticle]
  private var storageFuture: Future[StorageAsync] = _

  override protected val serverPort: Int = 8666

  override protected def initRouter(router: Router): Unit = {
    router.post("/api/signup").handler(handlerSignup)
    router.post("/api/signout").handler(handlerSignout)
    router.get("/api/login").handler(handlerLogin)
    router.get("/api/validate").handler(handlerValidation)
  }

  override protected def initServer: Future[_] = {
    storageFuture = vertx.fileSystem.readFileFuture("database/jdbc_config.json")
      .map(_.toJsonObject)
      .map(JDBCClient.createShared(vertx, _))
      .map(client => StorageAsync(client))
      .flatMap(storage => storage.init().map(_ => storage))
    storageFuture
  }

  private def sendError(statusCode: Int, response: HttpServerResponse): Unit = {
    logger.debug(s"Error! Invalid request. Answering $statusCode")
    response.setStatusCode(statusCode).end()
  }

  private def handlerSignup(routingContext: RoutingContext): Unit = {
    logger.debug("Received sign up request.")
    for (
      authorizationHeader <- routingContext.request.getAuthentication;
      (username, password) <- HttpUtils.readBasicAuthentication(authorizationHeader)
    ) yield {
      storageFuture flatMap (_.signupFuture(username, password)) onComplete {
        case Success(_) =>
          logger.info(s"User $username signed up.")
          JwtUtils
            .encodeUsernameToken(username)
            .foreach(token => routingContext.response() setStatusCode 201 end token)
        case Failure(_) => sendError(400, routingContext.response())
      }
      return
    }
    sendError(400, routingContext.response())
  }

  private def handlerSignout(routingContext: RoutingContext): Unit = {
    logger.debug("Received sign out request.")
    val response: HttpServerResponse = routingContext.response()
    for (
      authorizationHeader <- routingContext.request.getAuthentication;
      token <- HttpUtils.readJwtAuthentication(authorizationHeader);
      username <- JwtUtils.decodeUsernameToken(token)
    ) yield {
      // If every check pass, username contains the username contained in the token and we can check it exists
      storageFuture.map(_.signoutFuture(username).onComplete {
        case Success(_) =>
          logger.info(s"User $username signed out.")
          response setStatusCode 202 end
        case Failure(_) => sendError(401, response)
      })
      return
    }
    sendError(400, response)
  }

  private def handlerLogin(routingContext: RoutingContext): Unit = {
    logger.debug("Received login request.")
    for (
      authorizationHeader <- routingContext.request.getAuthentication;
      (username, password) <- HttpUtils.readBasicAuthentication(authorizationHeader)
    ) yield {
      storageFuture flatMap (_.loginFuture(username, password)) onComplete {
        case Success(_) =>
          logger.info(s"User $username logged in.")
          JwtUtils
            .encodeUsernameToken(username)
            .foreach(token => routingContext.response() setStatusCode 200 end token)
        case Failure(_) => sendError(401, routingContext.response())
      }
      return
    }
    sendError(400, routingContext.response())
  }

  private def handlerValidation(routingContext: RoutingContext): Unit = {
    logger.debug("Received token validation request.")
    for (
      authorizationHeader <- routingContext.request.getAuthentication;
      token <- HttpUtils.readJwtAuthentication(authorizationHeader);
      username <- JwtUtils.decodeUsernameToken(token)
    ) yield {
      // If every check pass, username contains the username contained in the token and we can check it exists
      storageFuture.map(_.existsFuture(username).onComplete {
        case Success(_) =>
          logger.info(s"Token validation for $username successful")
          routingContext.response() end username
        case Failure(_) => sendError(401, routingContext.response())
      })
      return
    }
    sendError(400, routingContext.response())
  }
}