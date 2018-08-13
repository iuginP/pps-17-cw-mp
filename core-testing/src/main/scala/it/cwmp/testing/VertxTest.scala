package it.cwmp.testing

import it.cwmp.services.VertxInstance
import org.scalatest.AsyncFunSpec

/**
  * A base test that provides a Vertx instance and its execution context
  *
  * @author Enrico Siboni
  */
abstract class VertxTest extends AsyncFunSpec with VertxInstance
