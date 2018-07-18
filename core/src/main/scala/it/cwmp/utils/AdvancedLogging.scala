package it.cwmp.utils

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}
import scala.util.{Failure, Success}

trait AdvancedLogging extends Loggable{

  implicit class RichFuture[T](future: Future[T]) {

    def logSuccessInfo(message: String)(implicit executionContext: ExecutionContext): Future[T] =
      future.andThen {
        case Success(_) => logger.info(message)
      }

    def logSuccessInfo(message: String, condition: T => Boolean)(implicit executionContext: ExecutionContext): Future[T] =
      future.andThen {
        case Success(s) if condition(s) => logger.info(message)
      }

    def logFailureInfo(message: String)(implicit executionContext: ExecutionContext): Future[T] =
      future.andThen {
        case Failure(e) => logger.info(message, e)
      }

    def logFailureInfo[A <: Exception: ClassTag](message: String)(implicit executionContext: ExecutionContext): Future[T] =
      future.andThen {
        case Failure(e) if classTag[A].runtimeClass.isInstance(e) => logger.info(message, e)
      }
  }
}
