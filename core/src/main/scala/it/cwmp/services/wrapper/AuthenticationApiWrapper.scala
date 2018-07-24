package it.cwmp.services.wrapper

import io.vertx.scala.ext.web.client.WebClientOptions
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.User
import it.cwmp.services.authentication.ServerParameters._
import it.cwmp.utils.{Validation, VertxClient, VertxInstance}

import scala.concurrent.Future

/**
  * A trait describing the API wrapper for authentication service
  */
trait AuthenticationApiWrapper extends Validation[String, User] {

  // TODO: doc
  def signUp(username: String, password: String): Future[String]

  def login(username: String, password: String): Future[String]
}

/**
  * Companion object
  */
object AuthenticationApiWrapper {

  val DEFAULT_HOST = "localhost"

  def apply(): AuthenticationApiWrapper =
    AuthenticationApiWrapper(DEFAULT_HOST, DEFAULT_PORT)

  def apply(host: String): AuthenticationApiWrapper =
    AuthenticationApiWrapper(host, DEFAULT_PORT)

  def apply(host: String, port: Int): AuthenticationApiWrapper =
    new AuthenticationApiWrapperImpl(WebClientOptions()
      .setDefaultHost(host)
      .setDefaultPort(port))

  /**
    * A default implementation class for Authentication API Wrapper
    */
  class AuthenticationApiWrapperImpl(override protected val clientOptions: WebClientOptions)
    extends AuthenticationApiWrapper with VertxInstance with VertxClient {

    def signUp(username: String, password: String): Future[String] =
      client.post(API_SIGNUP)
        .addAuthentication(username, password)
        .sendFuture()
        .expectStatus(201)
        .map(_.bodyAsString().getOrElse(""))

    override def login(username: String, password: String): Future[String] =
      client.get(API_LOGIN)
        .addAuthentication(username, password)
        .sendFuture()
        .expectStatus(200)
        .map(_.bodyAsString().getOrElse(""))

    override def validate(token: String): Future[User] =
      client.get(API_VALIDATE)
        .addAuthentication(token)
        .sendFuture()
        .expectStatus(200)
        .mapBody {
          case Some(body) => Future.successful(User(body))
          case _ => Future.failed(HTTPException(400, "Empty body"))
        }
  }

}
