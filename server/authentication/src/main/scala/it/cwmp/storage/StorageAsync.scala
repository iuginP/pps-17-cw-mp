package it.cwmp.storage

import io.vertx.core.json.JsonArray
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.sql.SQLConnection

import scala.concurrent._
import ExecutionContext.Implicits.global

trait StorageAsync {

  def signupFuture(client: JDBCClient, username: String, password: String): Future[Unit]

  def signoutFuture(client: JDBCClient, username: String): Future[Unit]

  def loginFuture(client: JDBCClient, username: String, password: String): Future[Unit]
}

object StorageAsync {

  def apply(): StorageAsync = new StorageAsyncImpl()

  class StorageAsyncImpl() extends StorageAsync {

    private def getConnection(client: JDBCClient): Future[SQLConnection] = {
      client.getConnectionFuture()
    }

    override def signupFuture(client: JDBCClient, username: String, password: String): Future[Unit] = {
      if (username == null || username.isEmpty
        || password == null || password.isEmpty) {
        Future.failed(new Exception())
      } else {
        getConnection(client).map(conn => {
          // create a test table
          conn.executeFuture("create table test(id int primary key, name varchar(255))").map(_ => {
            // insert some test data
            conn.executeFuture("insert into test values (1, 'Hello'), (2, 'World')").map(_ => {
              // query some data with arguments
              conn.queryWithParamsFuture("select * from test where id = ?", new JsonArray().add(2)).map(res => {
                res.getResults foreach println
                conn.close()
              })
            })
          })
        })
      }
    }

    override def signoutFuture(client: JDBCClient, username: String): Future[Unit] = ???

    override def loginFuture(client: JDBCClient, username: String, password: String): Future[Unit] = ???
  }
}