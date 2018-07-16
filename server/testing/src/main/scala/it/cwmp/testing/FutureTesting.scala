package it.cwmp.testing

import io.vertx.scala.ext.web.client.HttpResponse
import org.scalatest.{Assertion, Matchers}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case object FutureTestingException extends Exception

object FutureTesting {

  implicit class RichFutureTesting[T](future: Future[T]) extends Matchers {

    def shouldFail(implicit executionContext: ExecutionContext): Future[Assertion] = future
      .transform {
        case Failure(_) => Success(succeed)
        case _ => Failure(FutureTestingException)
      }

    def shouldSucceed(implicit executionContext: ExecutionContext): Future[Assertion] = future
      .map(_ => succeed)
  }

  implicit class RichWebFutureTesting[T](future: Future[HttpResponse[T]]) extends Matchers{

    def shouldRespondWith(statusCode: Int)(implicit executionContext: ExecutionContext): Future[Assertion] = future
      .map(response => assert(response.statusCode() == statusCode))

    def shouldRespondWith(statusCode: Int, strategy: Option[String] => Boolean)(implicit executionContext: ExecutionContext): Future[Assertion] = future
      .map(response => assert(response.statusCode() == statusCode && strategy(response.bodyAsString())))
  }
}
