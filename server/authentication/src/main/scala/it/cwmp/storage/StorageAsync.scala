package it.cwmp.storage

import io.vertx.core.{AsyncResult, Handler}
import io.vertx.scala.ext.jdbc.JDBCClient

import scala.concurrent.Future

trait StorageAsync {

  def signup(client: JDBCClient, username: String, password: String, handler: Handler[AsyncResult[Unit]]): Unit

  def signupFuture(client: JDBCClient, username: String, password: String): Future[Unit]

  def signout(client: JDBCClient, username: String, handler: Handler[AsyncResult[Unit]]): Unit

  def signoutFuture(client: JDBCClient, username: String): Future[Unit]

  def login(client: JDBCClient, username: String, password: String, handler: Handler[AsyncResult[Unit]]): Unit

  def loginFuture(client: JDBCClient, username: String, password: String): Future[Unit]
}

object StorageAsync {

  def apply(): StorageAsync = new StorageAsyncImpl()

  class StorageAsyncImpl() extends StorageAsync {

    override def signup(client: JDBCClient, username: String, password: String, handler: Handler[AsyncResult[Unit]]): Unit = ???

    override def signupFuture(client: JDBCClient, username: String, password: String): Future[Unit] = ???

    override def signout(client: JDBCClient, username: String, handler: Handler[AsyncResult[Unit]]): Unit = ???

    override def signoutFuture(client: JDBCClient, username: String): Future[Unit] = ???

    override def login(client: JDBCClient, username: String, password: String, handler: Handler[AsyncResult[Unit]]): Unit = ???

    override def loginFuture(client: JDBCClient, username: String, password: String): Future[Unit] = ???
  }
}