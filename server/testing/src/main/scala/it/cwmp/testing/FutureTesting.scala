package it.cwmp.testing

import io.vertx.scala.ext.web.client.HttpResponse
import org.scalatest.{Assertion, Matchers}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case object FutureTestingException extends Exception

/**
  * This is a utility object containing some implicit classes in order to provide useful verification tools
  * for [[Future]] and [[HttpResponse]].
  *
  * @author Eugenio Pierfederici
  */
object FutureTesting {

  implicit class RichFutureTesting[T](future: Future[T]) extends Matchers {

    /**
      * Asserts that the future has any sort of failure.
      * @param executionContext the implicit execution context
      * @return the future containing the result of the verification
      */
    def shouldFail(implicit executionContext: ExecutionContext): Future[Assertion] = future
      .transform {
        case Failure(_) => Success(succeed)
        case _ => Failure(FutureTestingException)
      }

    /**
      * Asserts that the future succeed.
      * @param executionContext the implicit execution context
      * @return the future containing the result of the verification
      */
    def shouldSucceed(implicit executionContext: ExecutionContext): Future[Assertion] = future
      .map(_ => succeed)
  }

  implicit class RichWebFutureTesting[T](future: Future[HttpResponse[T]]) extends Matchers{

    /**
      * Asserts that the future succeed (the response is returned) and that the server responded with the
      * specified status code.
      * @param statusCode the status code that the server should return
      * @param executionContext the implicit execution context
      * @return the future containing the result of the verification
      */
    def shouldRespondWith(statusCode: Int)(implicit executionContext: ExecutionContext): Future[Assertion] = future
      .map(response => assert(response.statusCode() == statusCode))

    /**
      * Asserts that the future succeed (the response is returned) and that the server responded with the
      * specified status code and that the body respects the rule specified in the strategy.
      * @param statusCode the status code that the server should return
      * @param strategy the strategy that should be used to validate the body
      * @param executionContext the implicit execution context
      * @return the future containing the result of the verification
      */
    def shouldRespondWith(statusCode: Int, strategy: Option[String] => Boolean)(implicit executionContext: ExecutionContext): Future[Assertion] = future
      .map(response => assert(response.statusCode() == statusCode && strategy(response.bodyAsString())))
  }
}
