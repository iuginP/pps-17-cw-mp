package it.cwmp.utils

import com.typesafe.scalalogging.Logger
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.HttpServerRequest
import io.vertx.scala.ext.web.Router

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait VertxServer extends ScalaVerticle {

  private val logger: Logger = Logger[VertxServer]

  protected def serverPort: Int

  protected def initRouter(router: Router): Unit

  protected def initServer: Future[_] = Future.successful(())

  override def startFuture(): Future[_] = {
    val router = Router.router(vertx)
    initRouter(router)
    initServer.flatMap(_ =>
      vertx.createHttpServer()
        .requestHandler(router.accept _)
        .listenFuture(serverPort))
    .andThen {
      case Success(_) => logger.info(s"RoomsService listening on port: $serverPort")
      case Failure(ex) => logger.error(s"Cannot start service on port: $serverPort", ex)
    }
  }

  /**
    * An implicit class to provide the [[HttpServerRequest]] with some more useful utilities.
    */
  import io.netty.handler.codec.http.HttpHeaderNames
  implicit class richHttpRequest(request: HttpServerRequest) {

    def getAuthentication: Option[String] = request.getHeader(HttpHeaderNames.AUTHORIZATION.toString)

    def isAuthorizedFuture(implicit strategy: Validation[String, Boolean]): Future[Boolean] = getAuthentication match {
      case None => Future.successful(false)
      case Some(authentication) => strategy.validate(authentication)
    }
  }
}