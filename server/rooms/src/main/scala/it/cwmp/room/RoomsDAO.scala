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
  * A trait that describes the Data Access Object for Rooms
  *
  * @author Enrico Siboni
  */
trait RoomsDAO {
  def initialize(): Future[Unit]

  def createRoom(roomID: String, roomName: String, neededPlayers: Int): Future[Unit]

  def listRooms(): Future[Seq[Room]]

  def enterPublicRoom(user: User): Future[Unit]

  def enterRoom(roomID: String, user: User): Future[Unit]

  def getRoomInfo(roomID: String): Future[Room]
}

/**
  * Companion object
  *
  * @author Enrico Siboni
  */
object RoomsDAO {

  def apply(vertx: Vertx): RoomsDAO = new RoomLocalDAO(vertx)


  /**
    * A wrapper to access a local Vertx storage for Rooms
    */
  private class RoomLocalDAO(vertx: Vertx) extends RoomsDAO {
    private val localJDBCClient = JDBCClient.createShared(vertx, localConfig)
    private implicit val executionContext: VertxExecutionContext = VertxExecutionContext(vertx.getOrCreateContext())

    private val publicRoomName: String = "public"

    override def initialize(): Future[Unit] = {
      localJDBCClient.getConnectionFuture().flatMap(conn =>
        conn.executeFuture(dropUserTableSql).flatMap(_ =>
          conn.executeFuture(dropRoomTableSql).flatMap(_ =>
            conn.executeFuture(createRoomTableSql).flatMap(_ =>
              conn.executeFuture(createUserTableSql).flatMap(_ =>
                conn.updateWithParamsFuture(insertNewRoomSql, Seq("ID", publicRoomName, "4")) // TODO: add other public rooms
                  .map(_ => conn.close())
              )
            )
          )
        )
      )
    }

    override def createRoom(roomID: String, roomName: String, neededPlayers: Int): Future[Unit] = {
      if (emptyString(roomID) || emptyString(roomName) || roomName == publicRoomName || neededPlayers < 1) {
        Future.failed(new Exception)
      } else {
        localJDBCClient.getConnectionFuture().flatMap(conn =>
          conn.updateWithParamsFuture(insertNewRoomSql, Seq(roomID, roomName, neededPlayers.toString))
            .map(_ => conn.close())
        )
      }
    }

    override def listRooms(): Future[Seq[Room]] = {
      localJDBCClient.getConnectionFuture().flatMap(conn => {
        conn.queryFuture(selectAllRoomsAndUsersSql)
          .map(resultSet => {
            //            resultSet.getResults.foreach(println(_))
            userTableRecordsToRooms(resultSet)
          })
      })
    }

    override def enterPublicRoom(user: User): Future[Unit] = {
      enterRoom("ID", user) // TODO: change the ID to be variable
    }

    override def enterRoom(roomID: String, user: User): Future[Unit] = {
      if (emptyString(roomID) || user == null) {
        Future.failed(new Exception)
      } else {
        localJDBCClient.getConnectionFuture().flatMap(conn => {
          conn.updateWithParamsFuture(insertUserInRoomSql, Seq(user.toString, roomID))
            .map(_ => conn.close())
        })
      }
    }

    override def getRoomInfo(roomID: String): Future[Room] = {
      listRooms().map(_.find(_.identifier == roomID).get)
    }

    private def localConfig: JsonObject = new JsonObject()
      .put("url", "jdbc:hsqldb:mem:test?shutdown=true")
      .put("driver_class", "org.hsqldb.jdbcDriver")
      .put("max_pool_size", 30)
      .put("user", "SA")
      .put("password", "")


    private val userToRoomLinkField = "user_room"
    private val createRoomTableSql =
      s"""
        CREATE TABLE room (
          ${Room.FIELD_IDENTIFIER} VARCHAR(100) NOT NULL,
          ${Room.FIELD_NAME} VARCHAR(50) NOT NULL,
          ${Room.FIELD_NEEDED_PLAYERS} INT NOT NULL,
          PRIMARY KEY (${Room.FIELD_IDENTIFIER})
        )
      """
    private val createUserTableSql =
      s"""
        CREATE TABLE user (
          ${User.FIELD_USERNAME} VARCHAR(50) NOT NULL,
          $userToRoomLinkField VARCHAR(100),
          PRIMARY KEY (${User.FIELD_USERNAME}),
          CONSTRAINT FK_userRooms FOREIGN KEY ($userToRoomLinkField) REFERENCES room(${Room.FIELD_IDENTIFIER})
        )
      """

    private val dropUserTableSql = "DROP TABLE user IF EXISTS"
    private val dropRoomTableSql = "DROP TABLE room IF EXISTS"
    private val insertNewRoomSql = "INSERT INTO room VALUES (?, ?, ?)"
    private val insertUserInRoomSql = "INSERT INTO user VALUES (?, ?)"
    private val selectAllRoomsAndUsersSql = s"SELECT * FROM room LEFT JOIN user ON ${Room.FIELD_IDENTIFIER} = $userToRoomLinkField"

    /**
      * Utility method to convert a result set to a room sequence
      */
    private def userTableRecordsToRooms(resultSet: ResultSet): Seq[Room] = {
      val roomsToUsers: mutable.Map[String, Seq[User]] = mutable.HashMap()
      val roomsInfo: mutable.Map[String, (String, Int)] = mutable.HashMap()
      resultSet.getResults.foreach(resultRow => {
        val roomID = resultRow.getString(0)
        val userName = if (resultRow.hasNull(3)) None else Some(resultRow.getString(3))

        if (roomsToUsers.contains(roomID)) {
          userName.foreach(name => roomsToUsers(roomID) = roomsToUsers(roomID) :+ User(name))
        } else {
          val roomName = resultRow.getString(1)
          val roomNeededPlayers = resultRow.getInteger(2)
          roomsInfo(roomID) = (roomName, roomNeededPlayers)
          userName match {
            case Some(name) => roomsToUsers(roomID) = Seq(User(name))
            case None => roomsToUsers(roomID) = Seq()
          }
        }
      })

      (for (
        roomUsers <- roomsToUsers;
        roomInfo <- roomsInfo if roomUsers._1 == roomInfo._1;
        room = Room(roomInfo._1, roomInfo._2._1, roomInfo._2._2, roomUsers._2)
      ) yield room).toSeq
    }

    /**
      * Implicit conversion to jsonArray
      *
      * @return the converted data
      */
    private implicit def stringsToJsonArray(arguments: Iterable[String]): JsonArray = {
      arguments.foldLeft(new JsonArray)(_.add(_))
    }

    /**
      * @return true if the string is empty or null
      */
    private def emptyString(string: String): Boolean = string == null || string.isEmpty
  }

}