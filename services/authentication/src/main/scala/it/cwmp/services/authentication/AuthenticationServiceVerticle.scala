package it.cwmp.services.authentication

import io.netty.handler.codec.http.HttpResponseStatus._
import io.vertx.core.Handler
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.cwmp.services.VertxServer
import it.cwmp.services.authentication.Service._
import it.cwmp.services.authentication.utils.JwtUtils
import it.cwmp.utils.Utils.{httpStatusNameToCode, stringToOption}
import it.cwmp.utils.{HttpUtils, Logging}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Class that implements the Authentication micro-service
  */
case class AuthenticationServiceVerticle(override protected val serverPort: Int = DEFAULT_PORT) extends VertxServer with Logging {

  private var storageFuture: Future[AuthenticationDAO] = _

  override protected def initRouter(router: Router): Unit = {
    router post API_SIGN_UP handler handlerSignUp
    router delete API_SIGN_OUT handler handlerSignOut
    router get API_LOGIN handler handlerLogin
    router get API_VALIDATE handler handlerValidation
  }

  override protected def initServer: Future[_] = {
    val storage = AuthenticationLocalDAO()
    storageFuture = storage.initialize().map(_ => storage)
    storageFuture
  }

  private def handlerSignUp: Handler[RoutingContext] = implicit routingContext => {
    log.debug("Received sign up request.")
    (for (
      authorizationHeader <- request.getAuthenticationHeader;
      (username, password) <- HttpUtils.readBasicAuthentication(authorizationHeader)
    ) yield {
      storageFuture flatMap (_.signUpFuture(username, password)) onComplete {
        case Success(_) =>
          log.info(s"User $username signed up.")
          JwtUtils
            .encodeUsernameToken(username)
            .foreach(sendResponse(CREATED, _))
        case Failure(_) => sendResponse(BAD_REQUEST)
      }
    }) orElse Some(sendResponse(BAD_REQUEST))
  }

  private def handlerSignOut: Handler[RoutingContext] = implicit routingContext => {
    log.debug("Received sign out request.")
    (for (
      authorizationHeader <- request.getAuthenticationHeader;
      token <- HttpUtils.readJwtAuthentication(authorizationHeader);
      username <- JwtUtils.decodeUsernameToken(token)
    ) yield {
      // If every check pass, username contains the username contained in the token and we can check it exists
      storageFuture.map(_.signOutFuture(username).onComplete {
        case Success(_) =>
          log.info(s"User $username signed out.")
          sendResponse(ACCEPTED)
        case Failure(_) => sendResponse(UNAUTHORIZED)
      })
    }) orElse Some(sendResponse(BAD_REQUEST))
  }

  private def handlerLogin: Handler[RoutingContext] = implicit routingContext => {
    log.debug("Received login request.")
    (for (
      authorizationHeader <- request.getAuthenticationHeader;
      (username, password) <- HttpUtils.readBasicAuthentication(authorizationHeader)
    ) yield {
      storageFuture flatMap (_.loginFuture(username, password)) onComplete {
        case Success(_) =>
          log.info(s"User $username logged in.")
          JwtUtils
            .encodeUsernameToken(username)
            .foreach(sendResponse(OK, _))
        case Failure(_) => sendResponse(UNAUTHORIZED)
      }
    }) orElse Some(sendResponse(BAD_REQUEST))
  }

  private def handlerValidation: Handler[RoutingContext] = implicit routingContext => {
    log.debug("Received token validation request.")
    (for (
      authorizationHeader <- request.getAuthenticationHeader;
      token <- HttpUtils.readJwtAuthentication(authorizationHeader);
      username <- JwtUtils.decodeUsernameToken(token)
    ) yield {
      // If every check pass, username contains the username contained in the token and we can check it exists
      storageFuture.map(_.existsFuture(username).onComplete {
        case Success(_) =>
          log.info(s"Token validation for $username successful")
          sendResponse(OK, username)
        case Failure(_) => sendResponse(UNAUTHORIZED)
      })
    }) orElse Some(sendResponse(BAD_REQUEST))
  }
}
