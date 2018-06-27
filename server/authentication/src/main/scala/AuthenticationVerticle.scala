import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.{HttpClient, HttpClientOptions, HttpServerResponse}
import io.vertx.scala.ext.web.Router

import scala.concurrent.{Future, Promise}

class AuthenticationVerticle extends ScalaVerticle {

  //host e porta del database al quale inoltriamo le richieste
  val host = "127.0.0.1"
  val port = 8667

  override def startFuture(): Future[_] = {

    //qui è dove ricevo le richieste
    val router = Router.router(vertx)

    def sendError(statusCode: Int,  response: HttpServerResponse): Unit = {
      response.setStatusCode(statusCode).end()
    }

    router.post("/api/singup").handler(handlerSingup)
    router.get("/api/login").handler(handlerLogin)
    router.get("/api/validate").handler(handlerVerification)

    def handlerSingup(routingContext: io.vertx.scala.ext.web.RoutingContext): Unit = {
      var userID = routingContext.request().getParam("userID")
      var password = routingContext.request().getParam("password")
      var response: HttpServerResponse = routingContext.response()

      if(routingContext.response() == null){
        sendError(400, response)
      } else {
        //TODO richiamare metodo gegio
      }
    }

    def handlerLogin(routingContext: io.vertx.scala.ext.web.RoutingContext): Unit = {
      var userID = routingContext.request().getParam("userID")
      var password = routingContext.request().getParam("password")
      var response: HttpServerResponse = routingContext.response()

      if(routingContext.response() == null){
        sendError(400, response)
      } else {
        //TODO richiamare metodo gegio
      }
    }

    def handlerVerification(routingContext: io.vertx.scala.ext.web.RoutingContext): Unit = {
      /*var userID = routingContext.request().getParam("userID")
      var password = routingContext.request().getParam("password")*/
      var response: HttpServerResponse = routingContext.response()

      if(routingContext.response() == null){
        sendError(400, response)
      } else {

        //TODO richiamare metodo gegio
      }
    }

    vertx
      .createHttpServer()
      .requestHandler(router.accept _)
      .listenFuture(8666, "0.0.0.0")
  }
}