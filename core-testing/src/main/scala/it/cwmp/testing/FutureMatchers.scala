package it.cwmp.testing

import io.vertx.scala.ext.web.client.HttpResponse
import org.scalatest.{Assertion, Matchers}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * This is a utility object containing some implicit classes in order to provide useful verification tools
  * for [[Future]] and [[HttpResponse]].
  *
  * @author Eugenio Pierfederici
  */
trait FutureMatchers {
  this: Matchers =>

  case object FutureTestingException extends Exception

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
      * Asserts that the future has the failure requested
      * @param executionContext the implicit execution context
      * @tparam T the type of failure that should be obtained
      * @return the future containing the result of the verification
      */
    def shouldFailWith[T <: Exception](implicit executionContext: ExecutionContext): Future[Assertion] = future
      .transform {
        case Failure(_: T) => Success(succeed)
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
}
