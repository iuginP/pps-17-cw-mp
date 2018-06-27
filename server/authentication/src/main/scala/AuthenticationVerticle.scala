import io.netty.handler.codec.http.HttpHeaders
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.HttpServerResponse
import io.vertx.scala.ext.web.{Router, RoutingContext}
import scala.concurrent.Future
import scala.util.{Failure, Success}

class AuthenticationVerticle extends ScalaVerticle {

  //host e porta del database al quale inoltriamo le richieste
  private val host = "127.0.0.1"
  private val port = 8667

  override def startFuture(): Future[_] = {

    //qui è dove ricevo le richieste
    val router = Router.router(vertx)

    router.post("/api/singup").handler(handlerSingup)
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

  private def handlerSingup(routingContext: RoutingContext): Unit = {
    //TODO leggere credenziali dall header

    val response = routingContext.response()

    if(response.headers().get(HttpHeaders.Names.AUTHORIZATION) == null){
      sendError(400, response)
    } else {
      val result: Future[Unit] = Future() //TODO implementare chiamata in db
      result andThen {
        case Success(s) => response end "TOKEN" // TODO Token generato con utente
        case Failure(f) => sendError(400, response)
      }
    }

    println("Authorization: " + response.headers().get("basic"))
  }

  private def handlerLogin(routingContext: io.vertx.scala.ext.web.RoutingContext): Unit = {
    //TODO leggere credenziali dall header
    var response: HttpServerResponse = routingContext.response()

    if(false /*param == null*/){
      sendError(400, response)
    } else {
      val result: Future[Unit] = Future() //TODO implementare chiamata in db
      result andThen {
        case Success(s) => response end "TOKEN" // TODO Token generato con utente
        case Failure(f) => sendError(400, response)
      }
    }
  }

  private def handlerVerification(routingContext: io.vertx.scala.ext.web.RoutingContext): Unit = {
    //TODO leggere credenziali dall header
    var response: HttpServerResponse = routingContext.response()

    if(false /*param == null*/){
      sendError(400, response)
    } else {
      val result: Future[Unit] = Future() //TODO implementare chiamata in db
      result andThen {
        case Success(s) => response end "TOKEN" // TODO Token generato con utente
        case Failure(f) => sendError(400, response)
      }
    }
  }
}