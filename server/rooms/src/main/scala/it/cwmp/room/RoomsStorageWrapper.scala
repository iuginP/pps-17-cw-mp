package it.cwmp.room

import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.json.JsonArray
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.sql.ResultSet
import it.cwmp.model.{Room, User}

import scala.collection.mutable
import scala.concurrent.Future

/**
  * A trait that describes the access to database made by rooms micro-service
  *
  * @author Enrico Siboni
  */
trait RoomsStorageWrapper {
  def initialize(): Future[Unit]

  def createRoom(name: String): Future[Unit]

  def listRooms(): Future[Seq[Room]]

  def enterPublicRoom(user: User): Future[Unit]

  def enterRoom(name: String, user: User): Future[Unit]

  def getRoomInfo(name: String): Future[Room]
}

/**
  * Companion object
  *
  * @author Enrico Siboni
  */
object RoomsStorageWrapper {
  def apply(vertx: Vertx): RoomsStorageWrapper = new RoomStorageLocalWrapper(vertx)


  /**
    * A wrapper to access a local Vertx storage
    */
  private class RoomStorageLocalWrapper(vertx: Vertx) extends RoomsStorageWrapper {
    private val localJDBCClient = JDBCClient.createShared(vertx, localConfig)
    private implicit val executionContext: VertxExecutionContext = VertxExecutionContext(vertx.getOrCreateContext())

    private val publicRoomName: String = "public"

    override def initialize(): Future[Unit] = {
      localJDBCClient.getConnectionFuture().flatMap(conn => {
        conn.executeFuture(createRoomTableSql).flatMap(_ => {
          conn.executeFuture(createUserTableSql).flatMap(_ => {
            conn.updateWithParamsFuture(insertNewRoomSql, new JsonArray().add(publicRoomName))
              .map(_ => conn.close())
          })
        })
      })
    }

    override def createRoom(name: String): Future[Unit] = {
      if (emptyString(name) || name == publicRoomName) {
        Future.failed(new Exception)
      } else {
        localJDBCClient.getConnectionFuture().flatMap(conn =>
          conn.updateWithParamsFuture(insertNewRoomSql, new JsonArray().add(name))
            .map(_ => conn.close())
        )
      }
    }

    override def listRooms(): Future[Seq[Room]] = {
      localJDBCClient.getConnectionFuture().flatMap(conn => {
        conn.queryFuture(selectAllRoomsAndUsersSql)
          .map(resultSet => {
            resultSet.getResults.foreach(println(_))
            resultSetToRooms(resultSet)
          })
      })
    }

    override def enterPublicRoom(user: User): Future[Unit] = {
      enterRoom(publicRoomName, user)
    }

    override def enterRoom(name: String, user: User): Future[Unit] = {
      if (emptyString(name) || user == null) {
        Future.failed(new Exception)
      } else {
        localJDBCClient.getConnectionFuture().flatMap(conn => {
          conn.updateWithParamsFuture(insertUserInRoomSql, (user, name))
            .map(_ => conn.close())
        })
      }
    }

    override def getRoomInfo(name: String): Future[Room] = {
      listRooms().map(rooms => rooms.find(room => room.name == name).get)
    }

    private def localConfig: JsonObject = new JsonObject()
      .put("url", "jdbc:hsqldb:mem:test?shutdown=true")
      .put("driver_class", "org.hsqldb.jdbcDriver")
      .put("max_pool_size", 30)
      .put("user", "SA")
      .put("password", "")

    private val createRoomTableSql =
      """
        CREATE TABLE room (
          room_name VARCHAR(45) NOT NULL,
          PRIMARY KEY (room_name)
        )
      """
    private val createUserTableSql =
      """
        CREATE TABLE user (
          user_username VARCHAR(45) NOT NULL,
          user_room VARCHAR(45),
          PRIMARY KEY (user_username),
          CONSTRAINT FK_userRooms FOREIGN KEY (user_room) REFERENCES room(room_name)
        )
      """

    private val insertNewRoomSql = "INSERT INTO room VALUES (?)"
    private val insertUserInRoomSql = "INSERT INTO user VALUES (?, ?)"
    private val selectAllRoomsAndUsersSql = "SELECT * FROM room LEFT JOIN user ON room_name = user_room"

    /**
      * Utility method to convert a result set to a room sequence
      */
    private def resultSetToRooms(resultSet: ResultSet): Seq[Room] = {
      val rooms: mutable.Map[String, Seq[User]] = mutable.HashMap()
      resultSet.getResults.foreach(resultRow => {
        val roomName = resultRow.getString(0)
        val userName = if (resultRow.hasNull(1)) None else Some(resultRow.getString(1))

        if (rooms.contains(roomName)) {
          userName.foreach(name =>
            rooms(roomName) = rooms(roomName) :+ User(name))
        } else {
          if (userName.isDefined) {
            rooms(roomName) = Seq(User(userName.get))
          } else {
            rooms(roomName) = Seq()
          }
        }
      })
      println(rooms)
      rooms.map(entry => Room(entry._1, entry._2)).toSeq
    }

    /**
      * Implicit conversion to jsonArray
      *
      * @return the converted data
      */
    private implicit def roomAndUserToJsonArray(roomAndUser: (User, String)): JsonArray = {
      new JsonArray().add(roomAndUser._1.username).add(roomAndUser._2)
    }

    private def emptyString(string: String): Boolean = string == null || string.isEmpty
  }

}