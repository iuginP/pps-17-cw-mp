package it.cwmp.services.authentication.storage

import io.vertx.core.json.JsonArray
import it.cwmp.utils.{VertxInstance, VertxJDBC}

import scala.concurrent._

trait StorageAsync {

  def init(): Future[Unit]

  def signupFuture(username: String, password: String): Future[Unit]

  def signoutFuture(username: String): Future[Unit]

  def loginFuture(username: String, password: String): Future[Unit]

  def existsFuture(username: String): Future[Unit]
}

object StorageAsync {

  def apply(): StorageAsync = new StorageAsyncImpl()

  class StorageAsyncImpl() extends StorageAsync with VertxInstance with VertxJDBC {

    override def init(): Future[Unit] =
      (for (
        connection <- openConnection();
        result <- connection.executeFuture(
          """
          CREATE TABLE IF NOT EXISTS authorization (
            auth_username VARCHAR(45) NOT NULL,
            auth_password VARCHAR(45) NOT NULL,
            auth_salt CHAR(32) NOT NULL,
            PRIMARY KEY (auth_username))
          """)
      ) yield result).closeConnections

    override def signupFuture(usernameP: String, passwordP: String): Future[Unit] =
      (for (
        // Controllo l'input
        username <- Option(usernameP) if username.nonEmpty;
        password <- Option(passwordP) if password.nonEmpty
      ) yield {
        (for (
          // Eseguo operazioni sul db in maniera sequenziale
          connection <- openConnection();
          _ <- connection.updateWithParamsFuture(
            """
              INSERT INTO authorization values (?, ?, ?)
              """, new JsonArray().add(username).add(password).add("SALT"))
        ) yield ()).closeConnections
      }).getOrElse(Future.failed(new IllegalArgumentException()))

    override def signoutFuture(usernameP: String): Future[Unit] =
      (for (
        // Controllo l'input
        username <- Option(usernameP) if username.nonEmpty
      ) yield {
        (for (
          // Eseguo operazioni sul db in maniera sequenziale
          connection <- openConnection();
          result <- connection.updateWithParamsFuture(
            """
            DELETE FROM authorization WHERE auth_username = ?
            """, new JsonArray().add(username)) if result.getUpdated > 0
        ) yield ()).closeConnections
      }).getOrElse(Future.failed(new IllegalArgumentException()))

    override def loginFuture(usernameP: String, passwordP: String): Future[Unit] =
      (for (
        // Controllo l'input
        username <- Option(usernameP) if username.nonEmpty;
        password <- Option(passwordP) if password.nonEmpty
      ) yield {
        (for (
          // Eseguo operazioni sul db in maniera sequenziale
          connection <- openConnection();
          result <- connection.queryWithParamsFuture(
            """
            SELECT *
            FROM authorization
            WHERE auth_username = ?
            AND auth_password = ?
            """, new JsonArray().add(username).add(password)) if result.getResults.nonEmpty
        ) yield ()).closeConnections
      }).getOrElse(Future.failed(new IllegalArgumentException()))


    override def existsFuture(usernameP: String): Future[Unit] =
      (for (
        // Controllo l'input
        username <- Option(usernameP) if username.nonEmpty
      ) yield {
        (for (
          // Eseguo operazioni sul db in maniera sequenziale
          connection <- openConnection();
          result <- connection.queryWithParamsFuture(
            """
            SELECT *
            FROM authorization
            WHERE auth_username = ?
            """, new JsonArray().add(username)) if result.getResults.nonEmpty
        ) yield ()).closeConnections
      }).getOrElse(Future.failed(new IllegalArgumentException()))

  }

}