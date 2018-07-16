package it.cwmp.testing

import io.vertx.lang.scala.ScalaVerticle
import org.scalatest.BeforeAndAfterAll

trait VerticleBeforeAndAfterAll extends VerticleTest with BeforeAndAfterAll {
  this: VertxTest =>

  /**
    * It contains the list of verticles to deploy before all the tests.
    * @return a [[Traversable]] containing all the verticles to deploy
    */
  protected def verticlesBeforeAll: Traversable[ScalaVerticle]

  override protected def beforeAll(): Unit =  {
    super.beforeAll()
    deployAll(verticlesBeforeAll)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    undeployAll()
  }
}
