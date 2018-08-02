package it.cwmp.services.authentication

import it.cwmp.services.authentication.AuthenticationLocalDAO._
import it.cwmp.utils.VertxJDBC.stringsToJsonArray
import it.cwmp.utils.{Logging, VertxInstance, VertxJDBC}

import scala.concurrent._

/**
  * Authentication Data Access object interface
  *
  * @author Davide Borficchia
  */
trait AuthenticationDAO {

  /**
    * Registers a new user
    *
    * @param username the username
    * @param password the password
    * @return a Future that completes when signUp succeeds
    */
  def signUpFuture(username: String, password: String): Future[Unit]

  /**
    * Performs user de-registration, that is a cancellation of its credentials from database
    *
    * @param username the username of user to delete
    * @return the Future that completes when signOut completes
    */
  def signOutFuture(username: String): Future[Unit]

  /**
    * Performs user log-in
    *
    * @param username the username
    * @param password the password
    * @return A Future that completes when login completed
    */
  def loginFuture(username: String, password: String): Future[Unit]

  /**
    * Checks existence of user inside database
    *
    * @param username the username to check
    * @return A Future that completes when user found
    */
  def existsFuture(username: String): Future[Unit]
}

/**
  * Default implementation of AuthenticationDAO
  *
  * @param configurationPath the database configuration file path
  * @author Davide Borficchia
  */
case class AuthenticationLocalDAO(override val configurationPath: Option[String] = Some("authentication/database.json"))
  extends AuthenticationDAO with VertxInstance with VertxJDBC with Logging {

  private var notInitialized = true

  /**
    * Initialization method; it should be called before using DAO
    *
    * @return A Future that completes when initialization successful
    */
  def initialize(): Future[Unit] = {
    log.info("Initializing RoomLocalDAO...")
    (for (
      connection <- openConnection();
      _ <- connection.executeFuture(createStorageTableSql)
    ) yield {
      notInitialized = false
    }).closeConnections
  }

  override def signUpFuture(usernameP: String, passwordP: String): Future[Unit] = {
    log.debug(s"signUp() username:$usernameP, password:$passwordP")
    (for (
      // Controllo l'input
      username <- Option(usernameP) if username.nonEmpty;
      password <- Option(passwordP) if password.nonEmpty
    ) yield {
      (for (
        // Eseguo operazioni sul db in maniera sequenziale
        _ <- checkInitialization(notInitialized);
        connection <- openConnection();
        _ <- connection.updateWithParamsFuture(
          insertNewUserSql, Seq(username, password, "SALT"))
      ) yield ()).closeConnections
    }).getOrElse(Future.failed(new IllegalArgumentException(USERNAME_OR_PASSWORD_NOT_PROVIDED_ERROR)))
  }

  override def signOutFuture(usernameP: String): Future[Unit] = {
    log.debug(s"signOutFuture() username:$usernameP")
    (for (
      // Controllo l'input
      username <- Option(usernameP) if username.nonEmpty
    ) yield {
      (for (
        // Eseguo operazioni sul db in maniera sequenziale
        _ <- checkInitialization(notInitialized);
        connection <- openConnection();
        result <- connection.updateWithParamsFuture(signOutUserSql, Seq(username)) if result.getUpdated > 0
      ) yield ()).closeConnections
    }).getOrElse(Future.failed(new IllegalArgumentException(USERNAME_NOT_PROVIDED_ERROR)))
  }

  override def loginFuture(usernameP: String, passwordP: String): Future[Unit] = {
    log.debug(s"loginFuture() username:$usernameP, password:$passwordP")
    (for (
      // Controllo l'input
      username <- Option(usernameP) if username.nonEmpty;
      password <- Option(passwordP) if password.nonEmpty
    ) yield {
      (for (
        // Eseguo operazioni sul db in maniera sequenziale
        _ <- checkInitialization(notInitialized);
        connection <- openConnection();
        result <- connection.queryWithParamsFuture(loginUserSql, Seq(username, password)) if result.getResults.nonEmpty
      ) yield ()).closeConnections
    }).getOrElse(Future.failed(new IllegalArgumentException(USERNAME_OR_PASSWORD_NOT_PROVIDED_ERROR)))
  }

  override def existsFuture(usernameP: String): Future[Unit] = {
    log.debug(s"existsFuture() username:$usernameP")
    (for (
      // Controllo l'input
      username <- Option(usernameP) if username.nonEmpty
    ) yield {
      (for (
        // Eseguo operazioni sul db in maniera sequenziale
        _ <- checkInitialization(notInitialized);
        connection <- openConnection();
        result <- connection.queryWithParamsFuture(existFutureUserSql, Seq(username)) if result.getResults.nonEmpty
      ) yield ()).closeConnections
    }).getOrElse(Future.failed(new IllegalArgumentException(USERNAME_NOT_PROVIDED_ERROR)))
  }
}

/**
  * Companion Object
  */
object AuthenticationLocalDAO {

  private val USERNAME_OR_PASSWORD_NOT_PROVIDED_ERROR = "Username or password not provided!"
  private val USERNAME_NOT_PROVIDED_ERROR = "Username not provided!"

  private val FIELD_AUTH_USERNAME = "auth_username"
  private val FIELD_AUTH_PASSWORD = "auth_password"
  private val FIELD_AUTH_SALT = "auth_salt"

  /**
    * Utility method to check if DAO was initialized
    *
    * @param notInitialized the flag to check for initialization
    * @return A succeeded Future if DAO was initialized, a failed Future otherwise
    */
  private def checkInitialization(notInitialized: Boolean): Future[Unit] = {
    if (notInitialized) Future.failed(new IllegalStateException("Not initialized, you should first call initialize()"))
    else Future.successful(())
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
  private val signOutUserSql = "DELETE FROM authorization WHERE auth_username = ?"
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