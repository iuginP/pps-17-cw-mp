package it.cwmp.room

import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.json.JsonArray
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.sql.{ResultSet, SQLConnection}
import it.cwmp.model.{Room, User}

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.Random

/**
  * A trait that describes the Data Access Object for Rooms
  *
  * @author Enrico Siboni
  */
trait RoomsDAO { // TODO: move in core... but not local implementation... then implement the communication with server there
  def initialize(): Future[Unit]

  def createRoom(roomName: String, playersNumber: Int): Future[String]

  def enterRoom(roomID: String)(implicit user: User): Future[Unit]

  def roomInfo(roomID: String): Future[Room]

  def exitRoom(roomID: String): Future[Unit]

  def listPublicRooms(): Future[Seq[Room]]

  def enterPublicRoom(playersNumber: Int)(implicit user: User): Future[Unit]

  def publicRoomInfo(playersNumber: Int): Future[Unit]

  def exitPublicRoom(playersNumber: Int): Future[Unit]
}

/**
  * Companion object
  *
  * @author Enrico Siboni
  */
object RoomsDAO {

  def apply(vertx: Vertx): RoomsDAO = new RoomLocalDAO(vertx)

  val publicPrefix: String = "public"

  /**
    * A wrapper to access a local Vertx storage for Rooms
    */
  private class RoomLocalDAO(vertx: Vertx) extends RoomsDAO {
    private val localJDBCClient = JDBCClient.createShared(vertx, localConfig)
    private var notInitialized = true
    private implicit val executionContext: VertxExecutionContext = VertxExecutionContext(vertx.getOrCreateContext())

    override def initialize(): Future[Unit] = {
      var randomIDs: Seq[Int] = Seq()
      // generate three different random IDs
      do randomIDs = for (_ <- 0 until 3; random = Random.nextInt(Int.MaxValue)) yield random
      while (randomIDs.distinct.size != 3)

      localJDBCClient.getConnectionFuture().flatMap(conn =>
        conn.executeFuture(s"$dropUserTableSql;$dropRoomTableSql")
          .flatMap(_ => conn.executeFuture(s"$createRoomTableSql;$createUserTableSql"))
          .flatMap(_ => conn.updateWithParamsFuture(insertNewRoomSql, Seq(s"$publicPrefix${randomIDs.head}", s"${publicPrefix}2", "2")))
          .flatMap(_ => conn.updateWithParamsFuture(insertNewRoomSql, Seq(s"$publicPrefix${randomIDs(1)}", s"${publicPrefix}3", "3")))
          .flatMap(_ => conn.updateWithParamsFuture(insertNewRoomSql, Seq(s"$publicPrefix${randomIDs(2)}", s"${publicPrefix}4", "4")))
          .map(_ => {
            conn.close()
            notInitialized = false
          })
      )
    }

    override def createRoom(roomName: String, playersNumber: Int): Future[String] = {
      checkInitialization().flatMap(_ =>
        if (emptyString(roomName)) Future.failed(new IllegalArgumentException("Room name empty"))
        else if (playersNumber < 2) Future.failed(new IllegalArgumentException(s"Room players number not valid: $playersNumber"))
        else
          localJDBCClient.getConnectionFuture().flatMap(conn =>
            getNotAlreadyPresentRoomID(conn).flatMap(roomID =>
              conn.updateWithParamsFuture(insertNewRoomSql, Seq(roomID, roomName, playersNumber.toString))
                .map(_ => {
                  conn.close()
                  roomID
                }))))
    }

    override def enterRoom(roomID: String)(implicit user: User): Future[Unit] = ???

    override def roomInfo(roomID: String): Future[Room] = ???

    override def exitRoom(roomID: String): Future[Unit] = ???

    override def listPublicRooms(): Future[Seq[Room]] = {
      checkInitialization().flatMap(_ =>
        localJDBCClient.getConnectionFuture().flatMap(conn =>
          conn.queryFuture(selectAllPublicRooms)
            .map(resultSet => {
              resultSet.getResults.foreach(println(_))
              userTableRecordsToRooms(resultSet)
            })))
    }

    override def enterPublicRoom(playersNumber: Int)(implicit user: User): Future[Unit] = ???

    override def publicRoomInfo(playersNumber: Int): Future[Unit] = ???

    override def exitPublicRoom(playersNumber: Int): Future[Unit] = ???


    //
    //    override def enterPublicRoom(user: User): Future[Unit] = {
    //      enterRoom("ID", user) // TODO: change the ID to be variable
    //    }
    //
    //    override def enterRoom(roomID: String, user: User): Future[Unit] = {
    //      if (emptyString(roomID) || user == null) {
    //        Future.failed(new Exception)
    //      } else {
    //        localJDBCClient.getConnectionFuture().flatMap(conn => {
    //          conn.updateWithParamsFuture(insertUserInRoomSql, Seq(user.toString, roomID))
    //            .map(_ => conn.close())
    //        })
    //      }
    //    }
    //
    //    override def getRoomInfo(roomID: String): Future[Room] = {
    //      listRooms().map(_.find(_.identifier == roomID).get)
    //    }

    private def localConfig: JsonObject = new JsonObject()
      .put("url", "jdbc:hsqldb:mem:test?shutdown=true")
      .put("driver_class", "org.hsqldb.jdbcDriver")
      .put("max_pool_size", 30)
      .put("user", "SA")
      .put("password", "")

    /**
      * Utility method to check if DAO is initialized
      */
    private def checkInitialization() = {
      if (notInitialized) Future.failed(new IllegalStateException("Not initialized, you should first call initialize()"))
      else Future.successful(Unit)
    }


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
    private val selectAllPublicRooms =
      s"""
         SELECT *
         FROM room LEFT JOIN user ON ${Room.FIELD_IDENTIFIER} = $userToRoomLinkField
         WHERE ${Room.FIELD_IDENTIFIER} LIKE '$publicPrefix%'
       """

    /**
      * Method to get a room random ID that isn't already in use
      *
      * @param connection the connection to DB where to check presence
      */
    private def getNotAlreadyPresentRoomID(connection: SQLConnection) = {
      val selectRoomIDSql = s"SELECT ${Room.FIELD_IDENTIFIER} FROM room WHERE ${Room.FIELD_IDENTIFIER} = ?"

      def _getNotAlreadyPresentRoomID(toCheckID: String, connection: SQLConnection): Future[String] =
        connection.queryWithParamsFuture(selectRoomIDSql, Seq(toCheckID)).flatMap(result => {
          result.getResults.size match {
            case 0 => Future.successful(toCheckID)
            case _ => _getNotAlreadyPresentRoomID(generateRandomRoomID(), connection)
          }
        })

      _getNotAlreadyPresentRoomID(generateRandomRoomID(), connection)
    }

    /**
      * Utility method to convert a result set to a room sequence
      */
    private def userTableRecordsToRooms(resultSet: ResultSet): Seq[Room] = { // review maybe with for comprehension
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
      * Method that generates a random room identifier
      */
    private def generateRandomRoomID() = Random.nextInt(Int.MaxValue).toString

    /**
      * @return true if the string is empty or null
      */
    private def emptyString(string: String): Boolean = string == null || string.isEmpty
  }

}