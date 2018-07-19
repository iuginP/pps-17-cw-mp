package it.cwmp.utils

import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.sql.SQLConnection

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

/**
  * This trait provides some utilities for an easier management of the JDBC connection with vertx.
  *
  * @author Eugenio Pierfederici
  */
trait VertxJDBC {
  this: VertxInstance =>

  private val clientFuture: Future[JDBCClient] = vertx.fileSystem.readFileFuture("database/jdbc_config.json")
    .map(_.toJsonObject)
    .map(JDBCClient.createShared(vertx, _))

  private var connectionList: ListBuffer[SQLConnection] = ListBuffer()

  /**
    * Open a connection and returns it.
    *
    * @return the connection just opened.
    */
  protected def openConnection(): Future[SQLConnection] = {
    clientFuture.flatMap(_.getConnectionFuture()).andThen {
      case Success(connection) => connectionList += connection
    }
  }

  /**
    * Closes the specified connection (or present as implicit)
    *
    * @param connection the connection we want to close.
    */
  protected def closeConnection(implicit connection: SQLConnection): Unit = {
    connectionList -= connection
    connection.close()
  }

  /**
    * Closes all the connections opened until now through this client.
    */
  protected def closeAllConnections(): Unit = connectionList.foreach(closeConnection(_))

  /**
    * This class decorates the future with some utils for the connection management.
    *
    * @param future the future to decorate
    * @tparam F the type of the future
    */
  implicit class RichFuture[F](future: Future[F]) {

    /**
      * When the future reaches that point, it closes all the connections opened until now in the client.
      *
      * @param executionContext the execution context in which to execute the operation
      * @return the future itself
      */
    def closeConnections(implicit executionContext: ExecutionContext): Future[F] =
      future.andThen {
        case _ => closeAllConnections()
      }
  }

}
