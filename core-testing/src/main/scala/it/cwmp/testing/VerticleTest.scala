package it.cwmp.testing

import io.vertx.lang.scala.ScalaVerticle

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * This class is automatically extended when using [[VerticleBeforeAndAfterEach]] or [[VerticleBeforeAndAfterAll]].
  * Here it is provided the structure for managing the deployment of the verticles and their undeploy at the end.
  *
  * @author Eugenio Pierfederici
  */
trait VerticleTest {
  this: VertxTest =>

  /**
    * Once deployed, this set contains all the ids of the verticles just started.
    */
  private var deployementIds: Set[String] = _

  /**
    * Deploys all the verticles passed as parameter. If any deploy fails or isn't deployed withing the
    * requested time, it throws a [[RuntimeException]]
    * @param verticles the list of verticle to deploy
    * @param atMost the maximum amount of time we can wait for the verticle to start. By default: 10000 milliseconds
    * @throws RuntimeException if any verticle can't be deployed
    */
  protected def deployAll(verticles: Traversable[ScalaVerticle], atMost: Duration = 10000.millis): Unit = {
    deployementIds = Set()
    verticles.foreach(verticle => deployementIds = deployementIds + Await.result(vertx.deployVerticleFuture(verticle)
      .andThen {
        case Success(d) => d
        case Failure(t) => throw new RuntimeException(t)
      }, atMost))
  }

  /**
    * Un-deploys all the verticles previously deployed. If any un-deploy fails or it isn't un-deployed withing the
    * requested time, it throws a [[RuntimeException]]
    * @param atMost the maximum amount of time we can wait for the verticle to stop. By default: 10000 milliseconds
    * @throws RuntimeException if any verticle can't be un-deployed
    */
  protected def undeployAll(atMost: Duration = 10000.millis): Unit =
    deployementIds.foreach(id => Await.result(vertx.undeployFuture(id)
      .andThen {
        case Success(d) => d
        case Failure(t) => throw new RuntimeException(t)
      }, atMost))
}
