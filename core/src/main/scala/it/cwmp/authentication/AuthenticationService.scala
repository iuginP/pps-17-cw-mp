package it.cwmp.authentication

import io.netty.handler.codec.http.HttpHeaderNames

import scala.concurrent._
import ExecutionContext.Implicits.global
import io.vertx.scala.ext.web.client.WebClient
import it.cwmp.utils.HttpUtils
import javax.xml.ws.http.HTTPException

import scala.concurrent.Future

trait AuthenticationService {

  def signUp(username: String, password: String): Future[String]

  def login(username: String, password: String): Future[String]

  def validate(token: String): Future[Unit]
}

object AuthenticationService {

  def apply(client: WebClient): AuthenticationService = new AuthenticationServiceImpl(client)

  class AuthenticationServiceImpl(client: WebClient) extends AuthenticationService {

    def signUp(username: String, password: String): Future[String] =
      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => Future.failed(new IllegalArgumentException())
        case Some(authHeader) => client.post("/api/signup")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            authHeader)
          .sendFuture()
          .map(res => res statusCode() match {
            case 201 => res.bodyAsString().get
            case code => throw new HTTPException(code)
          })
      }

    override def login(username: String, password: String): Future[String] =
      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => Future.failed(new IllegalArgumentException())
        case Some(authHeader) => client.get("/api/login")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            authHeader)
          .sendFuture()
          .map(res => res statusCode() match {
            case 200 => res.bodyAsString().get
            case code => throw new HTTPException(code)
          })
      }

    override def validate(token: String): Future[Unit] =
      HttpUtils.buildJwtAuthentication(token) match {
        case None => Future.failed(new IllegalArgumentException())
        case Some(authHeader) => client.get("/api/validate")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            authHeader)
          .sendFuture()
          .map(res => res statusCode() match {
            case 200 => res.bodyAsString().get
            case code => throw new HTTPException(code)
          })
      }
  }
}
