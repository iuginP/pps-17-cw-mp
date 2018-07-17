package it.cwmp.services.wrapper

import io.vertx.scala.ext.web.client.WebClientOptions
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.User
import it.cwmp.utils.{Validation, VertxClient, VertxInstance}

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait AuthenticationApiWrapper extends Validation[String, User] {

  def signUp(username: String, password: String): Future[String]

  def login(username: String, password: String): Future[String]
}

object AuthenticationApiWrapper {

  val DEFAULT_HOST = "localhost"
  val DEFAULT_PORT = 8666

  def apply(): AuthenticationApiWrapper =
    AuthenticationApiWrapper(DEFAULT_HOST, DEFAULT_PORT)

  def apply(host: String): AuthenticationApiWrapper =
    AuthenticationApiWrapper(host, DEFAULT_PORT)

  def apply(host: String, port: Int): AuthenticationApiWrapper =
    new AuthenticationApiWrapperImpl(WebClientOptions()
      .setDefaultHost(host)
      .setDefaultPort(port))

  class AuthenticationApiWrapperImpl(override protected val clientOptions: WebClientOptions)
    extends AuthenticationApiWrapper with VertxInstance with VertxClient {

    def signUp(username: String, password: String): Future[String] =
      client.post("/api/signup")
        .addAuthentication(username, password)
        .sendFuture()
        .transform({
          case Success(res) if res.statusCode() == 201 => Success(res.bodyAsString().get)
          case Success(res) => Failure(HTTPException(res.statusCode())) // TODO: add an error message as second argument of HTTP exception
          case Failure(f) => Failure(f)
        })

    override def login(username: String, password: String): Future[String] =
      client.get("/api/login")
        .addAuthentication(username, password)
        .sendFuture()
        .transform({
          case Success(res) if res.statusCode() == 200 => Success(res.bodyAsString().get)
          case Success(res) => Failure(HTTPException(res.statusCode())) // TODO: add an error message as second argument of HTTP exception
          case Failure(f) => Failure(f)
        })

    override def validate(token: String): Future[User] =
      client.get("/api/validate")
        .addAuthentication(token)
        .sendFuture()
        .transform {
          case Success(res) if res.statusCode() == 200 => Success(User(res.bodyAsString().get))
          case Success(res) => Failure(HTTPException(res.statusCode(), Some("Unauthorized user")))
          case Failure(f) => Failure(f)
        }
  }
}
