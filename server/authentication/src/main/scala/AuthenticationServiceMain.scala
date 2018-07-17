import io.vertx.scala.core.Vertx
import it.cwmp.services.authentication.AuthenticationServiceVerticle

/**
  * Hello class for server
  */
object AuthenticationServiceMain extends App {

  Vertx.vertx().deployVerticle(new AuthenticationServiceVerticle)

  println("Deploying AuthenticationServiceVerticle... ") // TODO replace with logger logging
}
