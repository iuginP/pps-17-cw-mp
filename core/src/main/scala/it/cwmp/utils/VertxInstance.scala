package it.cwmp.utils

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.scala.core.Vertx

/**
  * This trait represents a vertix instance.
  * It provides a vertx entry point and initialize the implicit execution context.
  * It should be mixed-in every time you need a vertx entry point.
  *
  * @author Eugenio Pierfederici
  */
trait VertxInstance {
  val vertx: Vertx = Vertx.vertx
  implicit val vertxExecutionContext: VertxExecutionContext = VertxExecutionContext(vertx.getOrCreateContext())
}
