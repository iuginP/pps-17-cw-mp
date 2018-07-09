package it.cwmp.testing

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.core.DeploymentOptions
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success}

/**
  * A base test class that deploys and undeploys a Verticle under testing
  *
  * @tparam A the type of the verticle to test
  */
abstract class VerticleTesting[A <: ScalaVerticle : TypeTag] extends VertxTest with BeforeAndAfterEach {

  private var deploymentId = ""

  def config(): JsonObject = Json.emptyObj()

  override protected def beforeEach(): Unit = {
    deploymentId = Await.result(
      vertx
        .deployVerticleFuture("scala:" + implicitly[TypeTag[A]].tpe.typeSymbol.fullName,
          DeploymentOptions().setConfig(config()))
        .andThen {
          case Success(d) => d
          case Failure(t) => throw new RuntimeException(t)
        },
      10000.millis
    )
  }

  override protected def afterEach(): Unit = {
    Await.result(
      vertx.undeployFuture(deploymentId)
        .andThen {
          case Success(d) => d
          case Failure(t) => throw new RuntimeException(t)
        },
      10000.millis
    )
  }
}
