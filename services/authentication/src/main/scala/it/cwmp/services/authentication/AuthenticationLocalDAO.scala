package it.cwmp.services.authentication

import it.cwmp.services.authentication.AuthenticationLocalDAO._
import it.cwmp.utils.VertxJDBC.stringsToJsonArray
import it.cwmp.utils.{Logging, VertxInstance, VertxJDBC}

import scala.concurrent._

/**
  * Trait che descrive l' Authentication Data Access Object
  *
  * @author Davide Borficchia
  */
trait AuthenticationDAO {
  /**
    * Registra un nuovo utente all'interno dello storage
    *
    * @param username username del nuovo utente
    * @param password password del nuovo utente
    * @return ritorna un Future vuoto
    */
  def signUpFuture(username: String, password: String): Future[Unit]

  /**
    * Fa sloggare un utente che ha precedentemente fatto login
    *
    * @param username username dell'utente che si vuole fare sloggare
    * @return ritorna un Future vuoto
    */
  def signOutFuture(username: String): Future[Unit]

  /**
    * Permette di far loggare un utente precedentemente registrato nel sistema
    *
    * @param username username dell'utente
    * @param password password dell'utente
    * @return ritorna un Future vuoto
    */
  def loginFuture(username: String, password: String): Future[Unit]

  /**
    * controlla se un utente è presente nel sistema
    *
    * @param username utente da controllare
    * @return ritorna un Future vuoto
    */
  def existsFuture(username: String): Future[Unit]
}

/**
  * Wrapper per accedere allo storage Vertex locale per l'autenticazione
  *
  * @author Davide Borficchia
  */
case class AuthenticationLocalDAO(override val configurationPath: String = "authentication/database.json")
  extends AuthenticationDAO with VertxInstance with VertxJDBC with Logging {

  private var notInitialized = true

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
    log.debug(s"signup() username:$usernameP, password:$passwordP")
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
      ) yield {
        log.debug("inizialized")
      }).closeConnections
    }).getOrElse(Future.failed(new IllegalArgumentException()))
  }

  override def signOutFuture(usernameP: String): Future[Unit] = {
    log.debug(s"signoutFuture() username:$usernameP")
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
    }).getOrElse(Future.failed(new IllegalArgumentException()))
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
    }).getOrElse(Future.failed(new IllegalArgumentException()))
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
    }).getOrElse(Future.failed(new IllegalArgumentException()))
  }
}

/**
  * Companion Object
  */
object AuthenticationLocalDAO {

  private val FIELD_AUTH_USERNAME = "auth_username"
  private val FIELD_AUTH_PASSWORD = "auth_password"
  private val FIELD_AUTH_SALT = "auth_salt"

  /**
    * Utility method per controlloare se il DAO è stato inizializzato
    */
  private def checkInitialization(notInitialized: Boolean): Future[Unit] = {
    if (notInitialized) Future.failed(new IllegalStateException("Not initialized, you should first call initialize()"))
    else Future.successful(Unit)
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