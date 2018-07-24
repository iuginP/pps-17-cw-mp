package it.cwmp.services.authentication

import com.typesafe.scalalogging.Logger
import io.vertx.core.json.JsonArray
import it.cwmp.services.authentication.StorageLoaclDAO._
import it.cwmp.utils.{VertxInstance, VertxJDBC}
import scala.concurrent._

trait StorageDAO {

  def signupFuture(username: String, password: String): Future[Unit]

  def signoutFuture(username: String): Future[Unit]

  def loginFuture(username: String, password: String): Future[Unit]

  def existsFuture(username: String): Future[Unit]
}

case class StorageLoaclDAO(override val configurationPath: String = "authentication/database.json") extends StorageDAO with VertxInstance with VertxJDBC {
  private var notInitialized = true

  def initialize(): Future[Unit] = {
    logger.info("Initializing RoomLocalDAO...")

    (for (
      connection <- openConnection();
      result <- connection.executeFuture(
        createStorageTableSql)
    ) yield {
      result
      notInitialized = false
    }).closeConnections
  }


  override def signupFuture(usernameP: String, passwordP: String): Future[Unit] = {
    logger.debug(s"signup() username:$usernameP, password:$passwordP")
    (for (
      // Controllo l'input
      username <- Option(usernameP) if username.nonEmpty;
      password <- Option(passwordP) if password.nonEmpty;
      future <- checkInitialization(notInitialized)
    ) yield {
      (for (
        // Eseguo operazioni sul db in maniera sequenziale
        connection <- openConnection();
        _ <- connection.updateWithParamsFuture(
          insertNewUserSql, new JsonArray().add(username).add(password).add("SALT"))
      ) yield {
        logger.debug("inizialized")
      }).closeConnections
    }).getOrElse(Future.failed(new IllegalArgumentException()))
  }


  override def signoutFuture(usernameP: String): Future[Unit] = {
    logger.debug(s"signoutFuture() username:$usernameP")
    (for (
      // Controllo l'input
      username <- Option(usernameP) if username.nonEmpty
    ) yield {
      (for (
        // Eseguo operazioni sul db in maniera sequenziale
        connection <- openConnection();
        result <- connection.updateWithParamsFuture(
          signoutUserSql, new JsonArray().add(username)) if result.getUpdated > 0
      ) yield ()).closeConnections
    }).getOrElse(Future.failed(new IllegalArgumentException()))
  }


  override def loginFuture(usernameP: String, passwordP: String): Future[Unit] = {
    logger.debug(s"loginFuture() username:$usernameP, password:$passwordP")
    (for (
      // Controllo l'input
      username <- Option(usernameP) if username.nonEmpty;
      password <- Option(passwordP) if password.nonEmpty
    ) yield {
      (for (
        // Eseguo operazioni sul db in maniera sequenziale
        connection <- openConnection();
        result <- connection.queryWithParamsFuture(
          loginUserSql, new JsonArray().add(username).add(password)) if result.getResults.nonEmpty
      ) yield ()).closeConnections
    }).getOrElse(Future.failed(new IllegalArgumentException()))
  }


  override def existsFuture(usernameP: String): Future[Unit] = {
    logger.debug(s"existsFuture() username:$usernameP")
    (for (
      // Controllo l'input
      username <- Option(usernameP) if username.nonEmpty
    ) yield {
      (for (
        // Eseguo operazioni sul db in maniera sequenziale
        connection <- openConnection();
        result <- connection.queryWithParamsFuture(
          existFutureUserSql, new JsonArray().add(username)) if result.getResults.nonEmpty
      ) yield ()).closeConnections
    }).getOrElse(Future.failed(new IllegalArgumentException()))
  }


}

object StorageLoaclDAO {
  private val logger: Logger = Logger[StorageLoaclDAO]
  private val FIELD_AUTH_USERNAME = "auth_username"
  private val FIELD_AUTH_PASSWORD = "auth_password"
  private val FIELD_AUTH_SALT = "auth_salt"

  /**
    * Utility method to check if DAO is initialized
    */
  private def checkInitialization(notInitialized: Boolean): Option[Future[Unit]] = {
    //if (notInitialized) Future.failed(new IllegalStateException("Not initialized, you should first call initialize()"))
    if (notInitialized) None //TODO implementare eccezione della riga sopra
    else Some(Future.successful(Unit))
  }

  private val createStorageTableSql =
    s"""
  CREATE TABLE IF NOT EXISTS authorization (
    $FIELD_AUTH_USERNAME VARCHAR(45) NOT NULL,
    $FIELD_AUTH_PASSWORD VARCHAR(45) NOT NULL,
    $FIELD_AUTH_SALT CHAR(32) NOT NULL,
    PRIMARY KEY ($FIELD_AUTH_USERNAME))
  """

  private val insertNewUserSql = "INSERT INTO authorization VALUES (?, ?, ?)"
  private val signoutUserSql = "DELETE FROM authorization WHERE auth_username = ?"
  private val loginUserSql =
    s"""
  SELECT *
  FROM authorization
  WHERE $FIELD_AUTH_USERNAME = ?
  AND $FIELD_AUTH_PASSWORD = ?
  """
  private val existFutureUserSql =
    s"""
  SELECT *
  FROM authorization
  WHERE $FIELD_AUTH_USERNAME = ?
  """
}