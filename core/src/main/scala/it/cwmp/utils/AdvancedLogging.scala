package it.cwmp.utils

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}
import scala.util.{Failure, Success}

/**
  * This trait provides a [[ch.qos.logback.classic.Logger]] as [[Logging]], but it has also a set of utility methods for logging.
  *
  * @author Eugenio Pierfederici
  */
trait AdvancedLogging extends Logging {

  /**
    * Enrich a future with logging capabilities.
    *
    * @param future the future to enrich.
    * @tparam T the type of value provided from the future.
    */
  implicit class RichFuture[T](future: Future[T]) {

    /**
      * Log an info message containing the text message if the future succeeds.
      *
      * @param message          the message to display.
      * @param executionContext the implicit execution context on which to execute the operation.
      * @return The future itself.
      */
    def logSuccessInfo(message: String)
                      (implicit executionContext: ExecutionContext): Future[T] =
      future.andThen {
        case Success(_) => log.info(message)
      }

    /**
      * Log an info message containing the text message if the future succeeds and the condition is respected.
      *
      * @param message          the message to display.
      * @param condition        the strategy that specifies if the argument of the successful future is right.
      * @param executionContext the implicit execution context on which to execute the operation.
      * @return The future itself.
      */
    def logSuccessInfo(message: String, condition: T => Boolean)
                      (implicit executionContext: ExecutionContext): Future[T] =
      future.andThen {
        case Success(s) if condition(s) => log.info(message)
      }

    /**
      * Log an info message containing the text message if the future fails.
      *
      * @param message          the message to display.
      * @param executionContext the implicit execution context on which to execute the operation.
      * @return The future itself.
      */
    def logFailureInfo(message: String)
                      (implicit executionContext: ExecutionContext): Future[T] =
      future.andThen {
        case Failure(e) => log.info(message, e)
      }

    /**
      * Log an info message containing the text message if the future fails with the specified exception type.
      *
      * @param message the message to display.
      * @tparam A the type of exception that should be returned as failure.
      * @param executionContext the implicit execution context on which to execute the operation.
      * @return The future itself.
      */
    def logFailureInfo[A <: Exception : ClassTag](message: String)
                                                 (implicit executionContext: ExecutionContext): Future[T] =
      future.andThen {
        case Failure(e) if classTag[A].runtimeClass.isInstance(e) => log.info(message, e)
      }
  }

}
