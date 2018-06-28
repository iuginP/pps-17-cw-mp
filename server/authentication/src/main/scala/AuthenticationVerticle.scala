import io.netty.handler.codec.http.{HttpHeaderNames}
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.HttpServerResponse
import io.vertx.scala.ext.web.{Router, RoutingContext}
import java.util.Base64
import scala.concurrent.Future
import scala.util.{Failure, Success}

class AuthenticationVerticle extends ScalaVerticle {

  //host e porta del database al quale inoltriamo le richieste
  private val host = "127.0.0.1"
  private val port = 8667

  override def startFuture(): Future[_] = {

    //qui è dove ricevo le richieste
    val router = Router.router(vertx)

    router.post("/api/signup").handler(handlersignup)
    router.get("/api/login").handler(handlerLogin)
    router.get("/api/validate").handler(handlerVerification)

    vertx
      .createHttpServer()
      .requestHandler(router.accept _)
      .listenFuture(8666, "0.0.0.0")
  }

  private def sendError(statusCode: Int,  response: HttpServerResponse): Unit = {
    response.setStatusCode(statusCode).end()
  }

  private def handlersignup(routingContext: RoutingContext): Unit = {
    val response = routingContext.response()
    val authorizationHeader = routingContext.request().headers().get(HttpHeaderNames.AUTHORIZATION toString)

    if(authorizationHeader == None){
      sendError(400, response)
    } else {
      val credentialFromHeader = authorizationHeader.get.split(" ")
      val textDecoded = new String(Base64.getDecoder.decode(credentialFromHeader(1))).split(":")
      val username = textDecoded(0)
      val password = textDecoded(1)
      println("Signup " + username, password)

      val result: Future[Unit] = Future() //TODO implementare chiamata in db
      result andThen {
        case Success(s) => response setStatusCode 201 end "TOKEN" // TODO Token generato con utente
        case Failure(f) => sendError(401, response)
      }
    }
  }

  private def handlerLogin(routingContext: io.vertx.scala.ext.web.RoutingContext): Unit = {
    var response: HttpServerResponse = routingContext.response()

    val authorizationHeader = routingContext.request().headers().get(HttpHeaderNames.AUTHORIZATION toString)

    if(authorizationHeader == None){
      sendError(400, response)
    } else {
      val credentialFromHeader = authorizationHeader.get.split(" ")
      val textDecoded = new String(Base64.getDecoder.decode(credentialFromHeader(1))).split(":")
      val username = textDecoded(0)
      val password = textDecoded(1)
      println("Login " + username, password)

      val result: Future[Unit] = Future() //TODO implementare chiamata in db
      result andThen {
        case Success(s) => response setStatusCode  201 end "TOKEN" // TODO Token generato con utente
        case Failure(f) => sendError(401, response)
      }
    }
  }

  private def handlerVerification(routingContext: io.vertx.scala.ext.web.RoutingContext): Unit = {
    var response: HttpServerResponse = routingContext.response()

    val authorizationHeader = routingContext.request().headers().get(HttpHeaderNames.AUTHORIZATION toString)

    if(authorizationHeader == None){
      sendError(400, response)
    } else {
      val tokenFromHeader = authorizationHeader.get.split(" ")
      val tokenDecoded = new String(Base64.getDecoder.decode(tokenFromHeader(1)))
      println(tokenDecoded)

      val result: Future[Unit] = Future() //TODO implementare chiamata in db
      result andThen {
        case Success(s) => response end "TOKEN" // TODO Token generato con utente
        case Failure(f) => sendError(401, response)
      }
    }
  }
}