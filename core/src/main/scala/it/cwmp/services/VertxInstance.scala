package it.cwmp.services

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.scala.core.Vertx

/**
  * This trait represents a vertx instance.
  * It provides a vertx entry point and initialize the implicit execution context.
  * It should be mixed-in every time you need a vertx entry point.
  *
  * @author Eugenio Pierfederici
  */
trait VertxInstance {
  private val vertxContext = Vertx.currentContext().getOrElse(Vertx.vertx.getOrCreateContext())

  val vertx: Vertx = vertxContext.owner()
  implicit val vertxExecutionContext: VertxExecutionContext = VertxExecutionContext(vertxContext)
}
