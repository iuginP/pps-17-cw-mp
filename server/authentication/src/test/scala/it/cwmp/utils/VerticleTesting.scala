package it.cwmp.utils

import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.lang.scala.{ScalaVerticle, VertxExecutionContext}
import io.vertx.scala.core.{DeploymentOptions, Vertx}
import org.scalatest.{AsyncFunSpec, BeforeAndAfter}

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success}

abstract class VerticleTesting[A <: ScalaVerticle: TypeTag] extends AsyncFunSpec with BeforeAndAfter{
  val vertx = Vertx.vertx
  implicit val vertxExecutionContext = VertxExecutionContext(
    vertx.getOrCreateContext()
  )

  private var deploymentId = ""

  def config(): JsonObject = Json.emptyObj()

  def shouldFail[A](future: Future[A]): Future[org.scalatest.compatible.Assertion] = {
    val promise = Promise[Unit]
    future.onComplete { case Failure(_) => promise.success() case Success(_) => promise.failure(_)}
    promise.future.map(_ => succeed)
  }

  before {
    deploymentId = Await.result(
      vertx
        .deployVerticleFuture("scala:" + implicitly[TypeTag[A]].tpe.typeSymbol.fullName,
          DeploymentOptions().setConfig(config()))
        .andThen {
          case Success(d) => d
          case Failure(t) => throw new RuntimeException(t)
        },
      10000 millis
    )
  }

  after {
    Await.result(
      vertx.undeployFuture(deploymentId)
        .andThen {
          case Success(d) => d
          case Failure(t) => throw new RuntimeException(t)
        },
      10000 millis
    )
  }

}
