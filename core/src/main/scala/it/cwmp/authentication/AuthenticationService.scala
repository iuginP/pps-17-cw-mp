package it.cwmp.authentication

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.User
import it.cwmp.utils.HttpUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait AuthenticationService extends HttpValidation[User] {

  def signUp(username: String, password: String): Future[String]

  def login(username: String, password: String): Future[String]
}

object AuthenticationService {

  val DEFAULT_HOST = "localhost"
  val DEFAULT_PORT = 8666

  def apply(): AuthenticationService =
    AuthenticationService(DEFAULT_HOST, DEFAULT_PORT)

  def apply(host: String): AuthenticationService =
    AuthenticationService(host, DEFAULT_PORT)

  def apply(host: String, port: Int): AuthenticationService =
    new AuthenticationServiceImpl(WebClient.create(Vertx.vertx,
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
            case Success(res) => Failure(HTTPException(res.statusCode())) // TODO: add an error message as second argument of HTTP exception
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
            case Success(res) => Failure(HTTPException(res.statusCode()))// TODO: add an error message as second argument of HTTP exception
            case Failure(f) => Failure(f)
          })
      }

    override def validate(token: String): Future[User] =
      HttpUtils.buildJwtAuthentication(token) match {
        case None => Future.failed(new IllegalArgumentException())
        case Some(authHeader) => verifyAuthorization(authHeader)
      }

    override def verifyAuthorization(authHeader: String): Future[User] =
      client.get("/api/validate")
          .putHeader(
            HttpHeaderNames.AUTHORIZATION.toString,
            authHeader)
          .sendFuture()
          .transform {
            case Success(res) if res.statusCode() == 200 => Success(User(res.bodyAsString().get))
            case Success(res) => Failure(HTTPException(res.statusCode(), Some("Unauthorized user")))
            case Failure(f) => Failure(f)
          }
  }

}
