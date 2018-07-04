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
import scala.language.implicitConversions
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

  def roomInfo(roomID: String): Future[Option[Room]]

  def exitRoom(roomID: String)(implicit user: User): Future[Unit]

  def listPublicRooms(): Future[Seq[Room]]

  def enterPublicRoom(playersNumber: Int)(implicit user: User): Future[Unit]

  def publicRoomInfo(playersNumber: Int): Future[Unit]

  def exitPublicRoom(playersNumber: Int)(implicit user: User): Future[Unit]
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

    private val EMPTY_ROOM_NAME_ERROR = "Room name must not be empty!"
    private val EMPTY_ROOM_ID_ERROR = "Provided room ID must not be empty!"
    private val INVALID_PLAYERS_NUMBER = "Players number invalid: "
    private val ALREADY_INSIDE_USER_ERROR = "The user is already inside a room: "
    private val NOT_INSIDE_USER_ERROR = "The user is not inside that room: "

    override def initialize(): Future[Unit] = {
      var randomIDs: Seq[Int] = Seq()
      // generate three different random IDs
      do randomIDs = for (_ <- 0 until 3; random = Random.nextInt(Int.MaxValue)) yield random
      while (randomIDs.distinct.size != 3)

      localJDBCClient.getConnectionFuture()
        .flatMap(conn => conn.executeFuture(dropUserTableSql)
          .flatMap(_ => conn.executeFuture(dropRoomTableSql))
          .flatMap(_ => conn.executeFuture(createRoomTableSql))
          .flatMap(_ => conn.executeFuture(createUserTableSql))
          .flatMap(_ => conn.updateWithParamsFuture(insertNewRoomSql, Seq(s"$publicPrefix${randomIDs.head}", s"${publicPrefix}2", "2")))
          .flatMap(_ => conn.updateWithParamsFuture(insertNewRoomSql, Seq(s"$publicPrefix${randomIDs(1)}", s"${publicPrefix}3", "3")))
          .flatMap(_ => conn.updateWithParamsFuture(insertNewRoomSql, Seq(s"$publicPrefix${randomIDs(2)}", s"${publicPrefix}4", "4")))
          .map(_ => notInitialized = false)
          .andThen { case _ => conn.close() }
        )
    }

    override def createRoom(roomName: String, playersNumber: Int): Future[String] = {
      checkInitialization()
        .flatMap(_ => emptyStringCheck(roomName, EMPTY_ROOM_NAME_ERROR))
        .flatMap(_ => playersNumberCheck(playersNumber, s"$INVALID_PLAYERS_NUMBER$playersNumber"))
        .flatMap(_ => localJDBCClient.getConnectionFuture())
        .flatMap(conn => getNotAlreadyPresentRoomID(conn)
          .flatMap(roomID => conn.updateWithParamsFuture(insertNewRoomSql, Seq(roomID, roomName, playersNumber.toString))
            .map(_ => roomID))
          .andThen { case _ => conn.close() }
        )
    }

    override def enterRoom(roomID: String)(implicit user: User): Future[Unit] = {
      checkInitialization()
        .flatMap(_ => emptyStringCheck(roomID, EMPTY_ROOM_ID_ERROR))
        .flatMap(_ => localJDBCClient.getConnectionFuture())
        .flatMap(conn => checkRoomPresence(roomID, conn)
          .flatMap(_ => getRoomOfUser(conn, user)) // check if user is inside any room
          .flatMap({
          case Some(room) => Future.failed(new IllegalArgumentException(s"$ALREADY_INSIDE_USER_ERROR${user.username} -> ${room.identifier}"))
          case None => Future.successful(Unit)
        })
          .flatMap(_ => conn.updateWithParamsFuture(insertUserInRoomSql, Seq(user.username, roomID)))
          .andThen { case _ => conn.close() }
          .map(_ => Unit)
        )
    }

    override def roomInfo(roomID: String): Future[Option[Room]] = {
      checkInitialization()
        .flatMap(_ => emptyStringCheck(roomID, EMPTY_ROOM_ID_ERROR))
        .flatMap(_ => localJDBCClient.getConnectionFuture())
        .flatMap(conn => conn.queryWithParamsFuture(selectRoomByIDSql, Seq(roomID))
          .map(resultSet => userTableRecordsToRooms(resultSet).headOption)
          .andThen { case _ => conn.close() }
        )
    }

    override def exitRoom(roomID: String)(implicit user: User): Future[Unit] = {
      checkInitialization()
        .flatMap(_ => emptyStringCheck(roomID, EMPTY_ROOM_ID_ERROR))
        .flatMap(_ => localJDBCClient.getConnectionFuture())
        .flatMap(conn => getRoomOfUser(conn, user) // check user inside room
          .flatMap({
          case Some(room) if room.identifier == roomID => Future.successful(Unit)
          case _ => Future.failed(new IllegalArgumentException(s"$NOT_INSIDE_USER_ERROR${user.username} -> $roomID"))
        })
          .flatMap(_ => conn.updateWithParamsFuture(deleteUserFormRoomSql, Seq(user.username)))
          .andThen { case _ => conn.close() }
          .map(_ => Unit)
        )
    }

    override def listPublicRooms(): Future[Seq[Room]] = {
      checkInitialization()
        .flatMap(_ => localJDBCClient.getConnectionFuture())
        .flatMap(conn => conn.queryFuture(selectAllPublicRooms)
          .andThen { case _ => conn.close() })
        .map(resultSet => userTableRecordsToRooms(resultSet))
    }

    override def enterPublicRoom(playersNumber: Int)(implicit user: User): Future[Unit] = checkInitialization()

    override def publicRoomInfo(playersNumber: Int): Future[Unit] = checkInitialization()

    override def exitPublicRoom(playersNumber: Int)(implicit user: User): Future[Unit] = checkInitialization()


    //
    //    override def enterPublicRoom(user: User): Future[Unit] = {
    //      enterRoom("ID", user) // TODO: change the ID to be variable
    //    }
    //
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
    private def checkInitialization(): Future[Unit] = {
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
    private val deleteUserFormRoomSql = s"DELETE FROM user WHERE ${User.FIELD_USERNAME} = ?"
    private val selectARoomIDSql = s"SELECT ${Room.FIELD_IDENTIFIER} FROM room WHERE ${Room.FIELD_IDENTIFIER} = ?"
    private val selectAllRooms =
      s"""
         SELECT *
         FROM room LEFT JOIN user ON ${Room.FIELD_IDENTIFIER} = $userToRoomLinkField
      """
    private val selectRoomByIDSql = s"$selectAllRooms WHERE ${Room.FIELD_IDENTIFIER} = ?"
    private val selectAllPublicRooms = s"$selectAllRooms WHERE ${Room.FIELD_IDENTIFIER} LIKE '$publicPrefix%'"
    private val selectRoomByUser = selectRoomByIDSql.replace("?",
      s"""
         (SELECT ${Room.FIELD_IDENTIFIER}
         FROM room LEFT JOIN user ON ${Room.FIELD_IDENTIFIER} = $userToRoomLinkField
         WHERE ${User.FIELD_USERNAME} = ?)
       """)


    /**
      * Method to get a room random ID that isn't already in use
      *
      * @param connection the connection to DB where to check presence
      */
    private def getNotAlreadyPresentRoomID(connection: SQLConnection): Future[String] = {

      def _getNotAlreadyPresentRoomID(toCheckID: String, connection: SQLConnection): Future[String] =
        checkRoomPresence(toCheckID, connection)
          .flatMap(_ => _getNotAlreadyPresentRoomID(generateRandomRoomID(), connection))
          .fallbackTo(Future.successful(toCheckID))

      _getNotAlreadyPresentRoomID(generateRandomRoomID(), connection)
    }

    /**
      * @return the future containing the room where optionally the user is
      */
    private def getRoomOfUser(connection: SQLConnection, user: User) = {
      connection.queryWithParamsFuture(selectRoomByUser, Seq(user.username))
        .map(result => userTableRecordsToRooms(result))
        .map(_.headOption)
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
      * @return a succeeded Future if string is ok, a failed Future otherwise
      */
    private def emptyStringCheck(toCheck: String, errorMessage: String): Future[Unit] =
      if (toCheck == null || toCheck.isEmpty) Future.failed(new IllegalArgumentException(errorMessage))
      else Future.successful(Unit)

    /**
      * @return a succeeded Future if playersNumber is correct, a failed Future otherwise
      */
    private def playersNumberCheck(playersNumber: Int, errorMessage: String): Future[Unit] =
      if (playersNumber < 2) Future.failed(new IllegalArgumentException(errorMessage))
      else Future.successful(Unit)

    /**
      * @return a succeeded future if the rome with given ID is present, a failed future otherwise
      */
    private def checkRoomPresence(roomID: String, connection: SQLConnection): Future[Unit] = {
      connection.queryWithParamsFuture(selectARoomIDSql, Seq(roomID))
        .flatMap(_.getResults.size match {
          case 0 => Future.failed(new NoSuchElementException)
          case _ => Future.successful(Unit)
        })
    }
  }

}