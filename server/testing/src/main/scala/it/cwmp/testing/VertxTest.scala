package it.cwmp.testing

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.scala.core.Vertx
import org.scalatest.AsyncFunSpec

/**
  * A base test that provides a Vertx instance and its execution context
  *
  * @author Enrico Siboni
  */
abstract class VertxTest extends AsyncFunSpec {
  val vertx: Vertx = Vertx.vertx
  implicit val vertxExecutionContext: VertxExecutionContext = VertxExecutionContext(vertx.getOrCreateContext())
}
