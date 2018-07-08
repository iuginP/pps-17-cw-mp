package it.cwmp.testing

import io.vertx.core.buffer.Buffer
import io.vertx.scala.ext.web.client.HttpResponse
import org.scalatest.Matchers
import org.scalatest.compatible.Assertion

import scala.concurrent.{ExecutionContext, Future}

/**
  * A trait that collects Http Custom Matchers for scalaTest
  *
  * @author Enrico Siboni
  */
trait HttpMatchers {
  this: Matchers =>

  /**
    * Implicit conversion to make easier to check status codes wrapped in futures and making them Future[Assertion]
    *
    * @param toCheck the response to check
    */
  implicit class VertxHttpResponseChecking(toCheck: Future[HttpResponse[Buffer]]) {
    def httpStatusCodeEquals(httpCode: Int)(implicit executionContext: ExecutionContext): Future[Assertion] =
      toCheck.map(_ statusCode() shouldBe httpCode)
  }

}
