package it.cwmp.testing

import io.vertx.scala.ext.web.client.HttpResponse
import org.scalatest.Matchers
import org.scalatest.compatible.Assertion

import scala.concurrent.{ExecutionContext, Future}

/**
  * A trait that collects Http Custom Matchers for scalaTest
  *
  * @author Enrico Siboni
  * @author Eugenio Pierfederici
  */
trait HttpMatchers {
  this: Matchers =>

  /**
    * Implicit conversion to make easier to check status codes wrapped in futures and making them Future[Assertion]
    *
    * @param toCheck the response to check
    */
  implicit class VertxHttpResponseChecking[T](toCheck: Future[HttpResponse[T]]) {

    /**
      * Asserts that the future succeed (the response is returned) and that the server responded with the
      * specified status code.
      * @param statusCode the status code that the server should return
      * @param executionContext the implicit execution context
      * @return the future containing the result of the verification
      */
    def shouldAnswerWith(statusCode: Int)(implicit executionContext: ExecutionContext): Future[Assertion] = toCheck
      .map(_.statusCode() shouldBe statusCode)

    /**
      * Asserts that the future succeed (the response is returned) and that the server responded with the
      * specified status code and that the body respects the rule specified in the strategy.
      * @param statusCode the status code that the server should return
      * @param strategy the strategy that should be used to validate the body
      * @param executionContext the implicit execution context
      * @return the future containing the result of the verification
      */
    def shouldAnswerWith(statusCode: Int, strategy: Option[String] => Boolean)(implicit executionContext: ExecutionContext): Future[Assertion] = toCheck
      .map(response => assert(response.statusCode() == statusCode && strategy(response.bodyAsString())))

  }
}
