package it.cwmp.storage

import io.vertx.core.json.JsonArray
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.sql.SQLConnection

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait StorageAsync {

  def init(): Future[Unit]

  def signupFuture(username: String, password: String): Future[Unit]

  def signoutFuture(username: String): Future[Unit]

  def loginFuture(username: String, password: String): Future[Unit]
}

object StorageAsync {

  def apply(client: JDBCClient): StorageAsync = new StorageAsyncImpl(client)

  class StorageAsyncImpl(client: JDBCClient) extends StorageAsync {

    private def getConnection(): Future[SQLConnection] = {
      client.getConnectionFuture()
    }

    override def init(): Future[Unit] = {
      getConnection().flatMap(conn => {
        // create a test table
        conn.executeFuture("""
          DROP TABLE authorization IF EXISTS;
          CREATE TABLE authorization (
            auth_username VARCHAR(45) NOT NULL,
            auth_password VARCHAR(45) NOT NULL,
            auth_salt CHAR(32) NOT NULL,
            PRIMARY KEY (auth_username))
          """).map(_ => {
          conn.close()
        })
      })
    }

    override def signupFuture(username: String, password: String): Future[Unit] = {
      if (username == null || username.isEmpty
        || password == null || password.isEmpty) {
        Future.failed(new Exception())
      } else {
        getConnection().flatMap(conn => {
          // insert the user in the authorization table
          conn.updateWithParamsFuture("""
            INSERT INTO authorization values (?, ?, ?)
            """, new JsonArray().add(username).add(password).add("SALT"))
            .map(res => {
              conn.close()
            })
        })
      }
    }

    override def signoutFuture(username: String): Future[Unit] = {
      if (username == null || username.isEmpty) {
        Future.failed(new Exception())
      } else {
        getConnection().flatMap(conn => {
          // insert the user in the authorization table
          conn.updateWithParamsFuture("""
            DELETE FROM authorization WHERE auth_username = ?
            """, new JsonArray().add(username)).map(_ => {
            conn.close()
          })
        })
      }
    }

    override def loginFuture(username: String, password: String): Future[Unit] = {
      if (username == null || username.isEmpty
        || password == null || password.isEmpty) {
        Future.failed(new Exception())
      } else {
        getConnection().flatMap(conn => {
          // check the user against the authorization table
          conn.queryWithParamsFuture("""
            SELECT *
            FROM authorization
            WHERE auth_username = ?
            AND auth_password = ?
            """, new JsonArray().add(username).add(password)).map(res => {
            conn.close()
            if (res.getResults.isEmpty) {
              throw new Exception
            }
          })
        })
      }
    }
  }
}