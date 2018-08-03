package it.cwmp.utils

import io.vertx.lang.scala.json.JsonArray
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.sql.SQLConnection

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Success

/**
  * This trait provides some utilities for an easier management of the JDBC connection with vertx.
  *
  * @author Eugenio Pierfederici
  */
trait VertxJDBC {
  this: VertxInstance =>

  def configurationPath: Option[String] = None

  private val DEFAULT_CONFIG_PATH: String = "database/jdbc_config.json"

  private val clientFuture: Future[JDBCClient] =
    for (
      configPath <- Future(configurationPath.getOrElse(DEFAULT_CONFIG_PATH));
      configExists <- vertx.fileSystem.existsFuture(configPath);
      configFileBuffer <- vertx.fileSystem.readFileFuture(if (configExists) configPath else DEFAULT_CONFIG_PATH)
    ) yield JDBCClient.createShared(vertx, configFileBuffer.toJsonObject)

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
    * Closes last opened connection through this client.
    */
  protected def closeLastOpenedConnection(): Unit =
    if (connectionList.nonEmpty) closeConnection(connectionList.last)

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
      future.andThen { case _ => closeAllConnections() }

    /**
      * When the future reaches this point, it closes the last opened connection till now in the client.
      *
      * @param executionContext the execution context in which to execute the operation
      * @return the future itself
      */
    def closeLastConnection(implicit executionContext: ExecutionContext): Future[F] =
      future.andThen { case _ => closeLastOpenedConnection() }
  }

}


/**
  * Companion object
  */
object VertxJDBC {

  /**
    * Implicit conversion to jsonArray
    *
    * @return the converted data
    */
  implicit def stringsToJsonArray(arguments: Iterable[String]): JsonArray = {
    arguments.foldLeft(new JsonArray)(_.add(_))
  }
}
