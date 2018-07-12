package it.cwmp.controller.rooms

import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.json.JsonArray
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.sql.{ResultSet, SQLConnection}
import it.cwmp.controller.rooms.RoomsLocalDAO._
import it.cwmp.model.{Address, Participant, Room, User}
import it.cwmp.utils.Utils

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Random

/**
  * A trait that describes the Rooms Data Access Object
  */
trait RoomDAO {

  /**
    * Creates a room
    *
    * @param roomName      the room name
    * @param playersNumber the players number
    * @return the future containing the identifier of the created room,
    *         or fails if roomName is empty or playersNumber not correct
    */
  def createRoom(roomName: String, playersNumber: Int): Future[String]

  /**
    * Enters a room
    *
    * @param roomID the identifier of the room
    * @param user   the user that wants to enter
    * @return the future that completes when the user has entered,
    *         or fails if roomID not provided, not present
    *         or user already inside a room, or room full
    */
  def enterRoom(roomID: String)(implicit user: Participant): Future[Unit]

  /**
    * Retrieves room information
    *
    * @param roomID the identifier of the room
    * @return the future that completes when the room information is available,
    *         or fails if room id not provided or not present
    */
  def roomInfo(roomID: String): Future[Room]

  /**
    * Exits a room
    *
    * @param roomID the identifier of the room to exit
    * @param user   the user that wants to exit
    * @return the future that completes when user has exited,
    *         or fails if roomId is not provided or not present
    *         or user is not inside that room
    */
  def exitRoom(roomID: String)(implicit user: User): Future[Unit]

  /**
    * retrieves a list of available public rooms
    *
    * @return a future that completes when such list is available
    */
  def listPublicRooms(): Future[Seq[Room]]

  /**
    * Enters a public room
    *
    * @param playersNumber the number of players that the public room has to have
    * @param user          the user that wants to enter
    * @return the future that completes when user ha entered,
    *         or fails if players number is not correct,
    *         or user already inside a room
    */
  def enterPublicRoom(playersNumber: Int)(implicit user: Participant): Future[Unit]

  /**
    * Retrieves information about a public room with specific number of players
    *
    * @param playersNumber the number of players that the public room has to have
    * @return the future that completes when the information is available,
    *         or fails if players number not correct
    */
  def publicRoomInfo(playersNumber: Int): Future[Room]

  /**
    * Exits a public room
    *
    * @param playersNumber the number of players that the public room has to have
    * @param user          the user that wants to exit
    * @return the future taht completes when the user has exited,
    *         or fails if players number is not correct,
    *         or user not inside that room
    */
  def exitPublicRoom(playersNumber: Int)(implicit user: User): Future[Unit]

  /**
    * Deletes a room if it's full
    *
    * @param roomID the id of the room to delete
    * @return the future that completes when deletion done,
    *         or fails if roomId is not provided or not present,
    *         of room is not full
    */
  def deleteRoom(roomID: String): Future[Unit]

  /**
    * Deletes a public room when it's full and recreates a new public room with same players number
    *
    * @param playersNumber the number of players that the public room has to have
    * @return the future that completes when the job is done,
    *         or fails if players number not correct,
    *         or that room is not full
    */
  def deleteAndRecreatePublicRoom(playersNumber: Int): Future[Unit]
}

/**
  * A wrapper to access a local Vertx storage for Rooms
  *
  * @author Enrico Siboni
  */
case class RoomsLocalDAO(localJDBCClient: JDBCClient, implicit val executionContext: VertxExecutionContext) extends RoomDAO {
  private var notInitialized = true

  private val PUBLIC_ROOM_MAX_SIZE = 4
  private val EMPTY_ROOM_NAME_ERROR = "Room name must not be empty!"
  private val EMPTY_ROOM_ID_ERROR = "Provided room ID must not be empty!"
  private val NOT_PRESENT_ROOM_ID_ERROR = "Provided id is not present!"
  private val ROOM_FULL_ERROR = "The room is full!"
  private val INVALID_PLAYERS_NUMBER = "Players number invalid: "
  private val ALREADY_INSIDE_USER_ERROR = "The user is already inside a room: "
  private val NOT_INSIDE_USER_ERROR = "The user is not inside that room: "
  private val DELETING_NON_FULL_ROOM_ERROR = "Cannot delete room if it's not full"

  import RoomsLocalDAO.stringsToJsonArray

  def initialize(): Future[Unit] = {
    localJDBCClient.getConnectionFuture()
      .flatMap(conn => conn.executeFuture(dropUserTableSql)
        .flatMap(_ => conn.executeFuture(dropRoomTableSql))
        .flatMap(_ => conn.executeFuture(createRoomTableSql))
        .flatMap(_ => conn.executeFuture(createUserTableSql))
        .map(_ =>
          for (playersNumber <- 2 to PUBLIC_ROOM_MAX_SIZE;
               creationFuture = createPublicRoom(conn, playersNumber)) yield creationFuture)
        .flatMap(Future.sequence(_)) // waits all creation future to end, or returns the failed one
        .map(_ => notInitialized = false)
        .andThen { case _ => conn.close() }
      )
  }

  override def createRoom(roomName: String, playersNumber: Int): Future[String] = {
    checkInitialization(notInitialized)
      .flatMap(_ => stringCheckFuture(roomName, EMPTY_ROOM_NAME_ERROR))
      .flatMap(_ => playersNumberCheck(playersNumber, s"$INVALID_PLAYERS_NUMBER$playersNumber"))
      .flatMap(_ => localJDBCClient.getConnectionFuture())
      .flatMap(conn => getNotAlreadyPresentRoomID(conn, generateRandomRoomID())
        .flatMap(roomID => conn.updateWithParamsFuture(insertNewRoomSql, Seq(roomID, roomName, playersNumber.toString))
          .map(_ => roomID))
        .andThen { case _ => conn.close() }
      )
  }

  override def enterRoom(roomID: String)(implicit user: Participant): Future[Unit] = {
    checkInitialization(notInitialized)
      .flatMap(_ => roomInfo(roomID))
      .flatMap(_ => localJDBCClient.getConnectionFuture())
      .flatMap(conn => checkRoomSpaceAvailable(roomID, conn, ROOM_FULL_ERROR)
        .flatMap(_ => getRoomOfUser(conn, user)) // check if user is inside any room
        .flatMap {
        case Some(room) => Future.failed(new IllegalStateException(s"$ALREADY_INSIDE_USER_ERROR${user.username} -> ${room.identifier}"))
        case None => Future.successful(Unit)
      }
        .flatMap(_ => conn.updateWithParamsFuture(insertUserInRoomSql, Seq(user.username, user.address, roomID)))
        .andThen { case _ => conn.close() })
      .map(_ => Unit)
  }

  override def roomInfo(roomID: String): Future[Room] = {
    checkInitialization(notInitialized)
      .flatMap(_ => stringCheckFuture(roomID, EMPTY_ROOM_ID_ERROR))
      .flatMap(_ => localJDBCClient.getConnectionFuture())
      .flatMap(conn => checkRoomPresence(roomID, conn, NOT_PRESENT_ROOM_ID_ERROR)
        .flatMap(_ => conn.queryWithParamsFuture(selectRoomByIDSql, Seq(roomID)))
        .map(resultOfJoinToRooms(_).head)
        .andThen { case _ => conn.close() })
  }

  override def exitRoom(roomID: String)(implicit user: User): Future[Unit] = {
    checkInitialization(notInitialized)
      .flatMap(_ => roomInfo(roomID))
      .flatMap(_ => localJDBCClient.getConnectionFuture())
      .flatMap(conn => getRoomOfUser(conn, user) // check user inside room
        .flatMap {
        case Some(room) if room.identifier == roomID => Future.successful(Unit)
        case _ => Future.failed(new IllegalStateException(s"$NOT_INSIDE_USER_ERROR${user.username} -> $roomID"))
      }
        .flatMap(_ => conn.updateWithParamsFuture(deleteUserFormRoomSql, Seq(user.username)))
        .andThen { case _ => conn.close() }
      )
      .map(_ => Unit)
  }

  override def listPublicRooms(): Future[Seq[Room]] = {
    checkInitialization(notInitialized)
      .flatMap(_ => localJDBCClient.getConnectionFuture())
      .flatMap(conn => conn.queryFuture(selectAllPublicRoomsSql)
        .andThen { case _ => conn.close() })
      .map(resultOfJoinToRooms)
  }

  override def enterPublicRoom(playersNumber: Int)(implicit user: Participant): Future[Unit] = {
    checkInitialization(notInitialized)
      .flatMap(_ => publicRoomIdFromPlayersNumber(playersNumber))
      .flatMap(enterRoom)
  }

  override def publicRoomInfo(playersNumber: Int): Future[Room] = {
    checkInitialization(notInitialized)
      .flatMap(_ => publicRoomIdFromPlayersNumber(playersNumber))
      .flatMap(roomInfo)
  }

  override def exitPublicRoom(playersNumber: Int)(implicit user: User): Future[Unit] = {
    checkInitialization(notInitialized)
      .flatMap(_ => publicRoomIdFromPlayersNumber(playersNumber))
      .flatMap(exitRoom)
  }

  override def deleteRoom(roomID: String): Future[Unit] = {
    checkInitialization(notInitialized)
      .flatMap(_ => roomInfo(roomID))
      .flatMap(room => localJDBCClient.getConnectionFuture()
        .flatMap(conn => checkRoomSpaceAvailable(roomID, conn, "")
          .flatMap(_ => Future.failed(new IllegalStateException(DELETING_NON_FULL_ROOM_ERROR))) // checking room full
          .recoverWith({ case ex: IllegalStateException if ex.getMessage.isEmpty => Future.successful(Unit) })

          // deleting should consist of removing users from room and then delete room
          .map(_ => for (user <- room.participants;
                         deleteFuture = conn.updateWithParamsFuture(deleteUserFormRoomSql, Seq(user.username))) yield deleteFuture)
          .flatMap(Future.sequence(_)) // waits for all to complete
          .flatMap(_ => conn.updateWithParamsFuture(deleteRoomSql, Seq(roomID)))
          .andThen { case _ => conn.close() }))
      .map(_ => Unit)
  }

  override def deleteAndRecreatePublicRoom(playersNumber: Int): Future[Unit] = {
    checkInitialization(notInitialized)
      .flatMap(_ => publicRoomIdFromPlayersNumber(playersNumber))
      .flatMap(deleteRoom)
      .flatMap(_ => localJDBCClient.getConnectionFuture())
      .flatMap(conn => createPublicRoom(conn, playersNumber)
        .andThen { case _ => conn.close() })
  }

  /**
    * @return the future containing the roomId of the public room with such players number,
    *         or failed future if players number too low or not present
    */
  private def publicRoomIdFromPlayersNumber(playersNumber: Int): Future[String] = {
    playersNumberCheck(playersNumber, s"$INVALID_PLAYERS_NUMBER$playersNumber")
      .flatMap(_ => listPublicRooms())
      .map(_.find(_.neededPlayersNumber == playersNumber))
      .flatMap {
        case Some(room) => Future.successful(room.identifier)
        case _ => Future.failed(new NoSuchElementException(s"$INVALID_PLAYERS_NUMBER$playersNumber"))
      }
  }
}


/**
  * Companion Object
  */
object RoomsLocalDAO {

  val publicPrefix: String = "public"

  /**
    * Utility method to check if DAO is initialized
    */
  private def checkInitialization(notInitialized: Boolean): Future[Unit] = {
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
          ${Participant.FIELD_ADDRESS} VARCHAR(255) NOT NULL,
          $userToRoomLinkField VARCHAR(100),
          PRIMARY KEY (${User.FIELD_USERNAME}),
          CONSTRAINT FK_userRooms FOREIGN KEY ($userToRoomLinkField) REFERENCES room(${Room.FIELD_IDENTIFIER})
        )
      """
  private val dropUserTableSql = "DROP TABLE user IF EXISTS"
  private val dropRoomTableSql = "DROP TABLE room IF EXISTS"

  private val insertNewRoomSql = "INSERT INTO room VALUES (?, ?, ?)"
  private val insertUserInRoomSql = "INSERT INTO user VALUES (?, ?, ?)"
  private val deleteUserFormRoomSql = s"DELETE FROM user WHERE ${User.FIELD_USERNAME} = ?"
  private val deleteRoomSql = s"DELETE FROM room WHERE ${Room.FIELD_IDENTIFIER} = ?"
  private val selectARoomIDSql = s"SELECT ${Room.FIELD_IDENTIFIER} FROM room WHERE ${Room.FIELD_IDENTIFIER} = ?"
  private val selectAllRoomsSql =
    s"""
         SELECT *
         FROM room LEFT JOIN user ON ${Room.FIELD_IDENTIFIER} = $userToRoomLinkField
      """
  private val selectRoomByIDSql = s"$selectAllRoomsSql WHERE ${Room.FIELD_IDENTIFIER} = ?"
  private val selectAllPublicRoomsSql = s"$selectAllRoomsSql WHERE ${Room.FIELD_IDENTIFIER} LIKE '$publicPrefix%'"
  private val selectRoomByUserSql = selectRoomByIDSql.replace("?",
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
  private def getNotAlreadyPresentRoomID(connection: SQLConnection, generator: => String)
                                        (implicit executionContext: VertxExecutionContext): Future[String] = {

    def _getNotAlreadyPresentRoomID(toCheckID: String, connection: SQLConnection): Future[String] =
      checkRoomPresence(toCheckID, connection, "")
        .flatMap(_ => _getNotAlreadyPresentRoomID(generator, connection))
        .fallbackTo(Future.successful(toCheckID))

    _getNotAlreadyPresentRoomID(generator, connection)
  }

  /**
    * @return the future containing the room where optionally the user is
    */
  private def getRoomOfUser(connection: SQLConnection, user: User)
                           (implicit executionContext: VertxExecutionContext) = {
    connection.queryWithParamsFuture(selectRoomByUserSql, Seq(user.username))
      .map(resultOfJoinToRooms(_).headOption)
  }

  /**
    * Utility method to convert a result set to a room sequence
    */
  private def resultOfJoinToRooms(resultSet: ResultSet): Seq[Room] = { // review maybe with for comprehension
    val roomsUsers: mutable.Map[String, Seq[Participant]] = mutable.HashMap()
    val roomsInfo: mutable.Map[String, (String, Int)] = mutable.HashMap()
    val roomIDPos = 0
    val roomNamePos = 1
    val roomPlayersPos = 2
    val userNamePos = 3
    val userAddressPos = 4

    for (resultRow <- resultSet.getResults) {
      val roomID = resultRow getString roomIDPos
      val userName = if (resultRow hasNull userNamePos) None else Some(resultRow getString userNamePos)
      val userAddress = if (resultRow hasNull userAddressPos) None else Some(resultRow getString userAddressPos)

      if (roomsUsers contains roomID) {
        userName.foreach(name => roomsUsers(roomID) = roomsUsers(roomID) :+ Participant(name, userAddress.get))
      } else {
        val roomName = resultRow getString roomNamePos
        val roomNeededPlayers = resultRow getInteger roomPlayersPos
        roomsInfo(roomID) = (roomName, roomNeededPlayers)
        userName match {
          case Some(name) => roomsUsers(roomID) = Seq(Participant(name, userAddress.get))
          case None => roomsUsers(roomID) = Seq()
        }
      }
    }

    (for (
      roomUsers <- roomsUsers;
      roomInfo <- roomsInfo if roomUsers._1 == roomInfo._1;
      room = Room(roomInfo._1, roomInfo._2._1, roomInfo._2._2, roomUsers._2)
    ) yield room).toSeq
  }

  /**
    * Implicit conversion to jsonArray
    *
    * @return the converted data
    */
  protected implicit def stringsToJsonArray(arguments: Iterable[String]): JsonArray = {
    arguments.foldLeft(new JsonArray)(_.add(_))
  }

  /**
    * Method that generates a random room identifier
    */
  private def generateRandomRoomID() = Random.nextInt(Int.MaxValue).toString

  /**
    * @return a succeeded Future if string is ok, a failed Future otherwise
    */
  private def stringCheckFuture(toCheck: String, errorMessage: String): Future[Unit] =
    Future {
      import Utils.emptyString
      require(!emptyString(toCheck), errorMessage)
    }

  /**
    * @return a succeeded Future if playersNumber is correct, a failed Future otherwise
    */
  private def playersNumberCheck(playersNumber: Int, errorMessage: String): Future[Unit] =
    if (playersNumber < 2) Future.failed(new IllegalArgumentException(errorMessage))
    else Future.successful(Unit)

  /**
    * @return a succeeded future if the room with given ID is present, a failed future otherwise
    */
  private def checkRoomPresence(roomID: String,
                                connection: SQLConnection,
                                errorMessage: String)(implicit executionContext: VertxExecutionContext): Future[Unit] = {
    connection.queryWithParamsFuture(selectARoomIDSql, Seq(roomID))
      .flatMap(_.getResults.size match {
        case 0 => Future.failed(new NoSuchElementException(errorMessage))
        case _ => Future.successful(Unit)
      })
  }

  /**
    * Internal method to create public rooms
    *
    * @return the future that completes when the room is created
    */
  private def createPublicRoom(connection: SQLConnection,
                               playersNumber: Int)(implicit executionContext: VertxExecutionContext): Future[Unit] = {
    getNotAlreadyPresentRoomID(connection, s"$publicPrefix${generateRandomRoomID()}")
      .flatMap(publicRoomID =>
        connection.updateWithParamsFuture(insertNewRoomSql, Seq(publicRoomID, s"$publicPrefix$playersNumber", playersNumber.toString)))
      .map(_ => Unit)
  }

  /**
    * @return a succeeded future if the room has available space for other users
    */
  private def checkRoomSpaceAvailable(roomID: String,
                                      connection: SQLConnection,
                                      errorMessage: String)(implicit executionContext: VertxExecutionContext): Future[Unit] = {
    connection.queryWithParamsFuture(selectRoomByIDSql, Seq(roomID))
      .map(result => resultOfJoinToRooms(result))
      .flatMap(_.head match {
        case Room(_, _, playersNumber, actualParticipants)
          if playersNumber > actualParticipants.size => Future.successful(Unit)
        case _ => Future.failed(new IllegalStateException(errorMessage))
      })
  }
}
