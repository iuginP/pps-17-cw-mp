package it.cwmp.authentication

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.model.User
import it.cwmp.utils.HttpUtils
import javax.xml.ws.http.HTTPException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait AuthenticationService extends Validation[String, User] {

  def signUp(username: String, password: String): Future[String]

  def login(username: String, password: String): Future[String]
}

object AuthenticationService {

  val DEFAULT_HOST = "localhost"
  val DEFAULT_PORT = 8666

  def apply()(implicit vertx: Vertx): AuthenticationService =
    AuthenticationService(DEFAULT_HOST, DEFAULT_PORT)

  def apply(host: String)(implicit vertx: Vertx): AuthenticationService =
    AuthenticationService(host, DEFAULT_PORT)

  def apply(host: String, port: Int)(implicit vertx: Vertx): AuthenticationService =
    new AuthenticationServiceImpl(WebClient.create(vertx,
      WebClientOptions()
        .setDefaultHost(host)
        .setDefaultPort(port)))

  class AuthenticationServiceImpl(client: WebClient) extends AuthenticationService {

    def signUp(username: String, password: String): Future[String] =
      HttpUtils.buildBasicAuthentication(username, password) match {
        case None => Future.failed(new IllegalArgumentException())
        case Some(authHeader) => client.post("/api/signup")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            authHeader)
          .sendFuture()
          .transform({
            case Success(res) if res.statusCode() == 201 => Success(res.bodyAsString().get)
            case Success(res) => Failure(new HTTPException(res.statusCode()))
            case Failure(f) => Failure(f)
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
          .transform({
            case Success(res) if res.statusCode() == 200 => Success(res.bodyAsString().get)
            case Success(res) => Failure(new HTTPException(res.statusCode()))
            case Failure(f) => Failure(f)
          })
      }

    override def validate(token: String): Future[User] =
      HttpUtils.buildJwtAuthentication(token) match {
        case None => Future.failed(new IllegalArgumentException())
        case Some(authHeader) => client.get("/api/validate")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            authHeader)
          .sendFuture()
          .transform({
            case Success(res) if res.statusCode() == 200 => Success(User(res.bodyAsString().get))
            case Success(res) => Failure(new HTTPException(res.statusCode()))
            case Failure(f) => Failure(f)
          })
      }
  }

}
