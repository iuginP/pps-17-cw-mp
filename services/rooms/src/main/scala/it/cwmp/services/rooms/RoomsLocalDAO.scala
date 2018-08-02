package it.cwmp.services.rooms

import io.vertx.scala.ext.sql.{ResultSet, SQLConnection}
import it.cwmp.model.{Address, Participant, Room, User}
import it.cwmp.services.rooms.RoomsLocalDAO._
import it.cwmp.utils.Utils.emptyString
import it.cwmp.utils.VertxJDBC.stringsToJsonArray
import it.cwmp.utils.{Logging, Utils, VertxInstance, VertxJDBC}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

/**
  * A trait that describes the Rooms Data Access Object
  *
  * @author Enrico Siboni
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
    * @param roomID              the identifier of the room
    * @param user                the user that wants to enter
    * @param notificationAddress the address where the user wants to receive other players info
    * @return the future that completes when the user has entered,
    *         or fails if roomID not provided, not present
    *         or user already inside a room, or room full
    */
  def enterRoom(roomID: String)
               (implicit user: Participant, notificationAddress: Address): Future[Unit]

  /**
    * Retrieves room information, and related user notification addresses
    *
    * @param roomID the identifier of the room
    * @return the future that completes when the room information is available,
    *         or fails if room id not provided or not present
    */
  def roomInfo(roomID: String): Future[(Room, Seq[Address])]

  /**
    * Exits a room
    *
    * @param roomID the identifier of the room to exit
    * @param user   the user that wants to exit
    * @return the future that completes when user has exited,
    *         or fails if roomId is not provided or not present
    *         or user is not inside that room
    */
  def exitRoom(roomID: String)
              (implicit user: User): Future[Unit]

  /**
    * retrieves a list of available public rooms
    *
    * @return a future that completes when such list is available
    */
  def listPublicRooms(): Future[Seq[Room]]

  /**
    * Enters a public room
    *
    * @param playersNumber       the number of players that the public room has to have
    * @param user                the user that wants to enter
    * @param notificationAddress the address where the user wants to receive other players info
    * @return the future that completes when user ha entered,
    *         or fails if players number is not correct,
    *         or user already inside a room
    */
  def enterPublicRoom(playersNumber: Int)
                     (implicit user: Participant, notificationAddress: Address): Future[Unit]

  /**
    * Retrieves information about a public room with specific number of players, and relative user notification addresses
    *
    * @param playersNumber the number of players that the public room has to have
    * @return the future that completes when the information is available,
    *         or fails if players number not correct
    */
  def publicRoomInfo(playersNumber: Int): Future[(Room, Seq[Address])]

  /**
    * Exits a public room
    *
    * @param playersNumber the number of players that the public room has to have
    * @param user          the user that wants to exit
    * @return the future that completes when the user has exited,
    *         or fails if players number is not correct,
    *         or user not inside that room
    */
  def exitPublicRoom(playersNumber: Int)
                    (implicit user: User): Future[Unit]

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
case class RoomsLocalDAO(override val configurationPath: Option[String] = Some("rooms/database.json")) extends RoomDAO
  with VertxInstance with VertxJDBC with Logging {

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

  /**
    * Initializes LocalDAO
    *
    * @return a Future that completes when DAO is initialized, or fails if an error occurs
    */
  def initialize(): Future[Unit] = {
    log.info("Initializing RoomLocalDAO...")

    def createDefaultPublicRooms(conn: SQLConnection) =
      Future.sequence { // waits all creation future to end, or returns the failed one
        for (playersNumber <- 2 to PUBLIC_ROOM_MAX_SIZE;
             creationFuture = createPublicRoom(conn, playersNumber)) yield creationFuture
      }

    (for (
      conn <- openConnection();
      _ <- conn.executeFuture(createRoomTableSql);
      _ <- conn.executeFuture(createUserTableSql);
      _ = notInitialized = false;
      publicRooms <- listPublicRooms() if publicRooms.isEmpty;
      _ = log.info(s"No public rooms found, creating public ones with players from 2 to $PUBLIC_ROOM_MAX_SIZE.");
      _ <- createDefaultPublicRooms(conn)
    ) yield ())
      .recover { case _: NoSuchElementException => log.info("Room database already exists, skipping creation.") }
      .closeConnections
  }

  override def createRoom(roomName: String, playersNumber: Int): Future[String] = {
    log.debug(s"createRoom() roomName:$roomName, playersNumber:$playersNumber")
    (for (
      _ <- checkInitialization(notInitialized);
      _ <- stringCheckFuture(roomName, EMPTY_ROOM_NAME_ERROR);
      _ <- playersNumberCheck(playersNumber, s"$INVALID_PLAYERS_NUMBER$playersNumber");
      conn <- openConnection();
      roomID <- getNotAlreadyPresentRoomID(conn, generateRandomRoomID());
      _ <- conn.updateWithParamsFuture(insertNewRoomSql, Seq(roomID, roomName, playersNumber.toString))
    ) yield roomID).closeLastConnection
  }

  override def enterRoom(roomID: String)
                        (implicit user: Participant, notificationAddress: Address): Future[Unit] = {

    def giveErrorOnRoomPresent(roomOption: Option[Room]): Future[Unit] = roomOption match {
      case Some(room) => Future.failed(new IllegalStateException(s"$ALREADY_INSIDE_USER_ERROR${user.username} -> ${room.identifier}"))
      case None => Future.successful(())
    }

    log.debug(s"enterRoom() roomID:$roomID, user:$user, notificationAddress:$notificationAddress")
    (for (
      _ <- checkInitialization(notInitialized);
      _ <- roomInfo(roomID);
      conn <- openConnection();
      _ <- checkRoomSpaceAvailable(roomID, conn, ROOM_FULL_ERROR);
      roomOption <- getRoomOfUser(conn, user); // check if user is inside any room
      _ <- giveErrorOnRoomPresent(roomOption);
      _ <- conn.updateWithParamsFuture(insertUserInRoomSql, Seq(user.username, user.address, notificationAddress.address, roomID))
    ) yield ()).closeLastConnection
  }

  override def roomInfo(roomID: String): Future[(Room, Seq[Address])] = {
    log.debug(s"roomInfo() roomID:$roomID")
    (for (
      _ <- checkInitialization(notInitialized);
      _ <- stringCheckFuture(roomID, EMPTY_ROOM_ID_ERROR);
      conn <- openConnection();
      _ <- checkRoomPresence(roomID, conn, NOT_PRESENT_ROOM_ID_ERROR);
      roomResult <- conn.queryWithParamsFuture(selectRoomByIDSql, Seq(roomID))
    ) yield RoomsDatabaseResultsManager.resultOfJoinToRoomsWithAddresses(roomResult).head).closeLastConnection
  }

  override def exitRoom(roomID: String)
                       (implicit user: User): Future[Unit] = {

    def giveErrorOnWrongOrNotPresentRoom(roomOption: Option[Room], toCheckRoomID: String): Future[Unit] = roomOption match {
      case Some(room) if room.identifier == toCheckRoomID => Future.successful(())
      case _ => Future.failed(new IllegalStateException(s"$NOT_INSIDE_USER_ERROR${user.username} -> $toCheckRoomID"))
    }

    log.debug(s"exitRoom() roomID:$roomID, user:$user")
    (for (
      _ <- checkInitialization(notInitialized);
      _ <- roomInfo(roomID);
      conn <- openConnection();
      roomOption <- getRoomOfUser(conn, user); // check user inside room
      _ <- giveErrorOnWrongOrNotPresentRoom(roomOption, roomID);
      _ <- conn.updateWithParamsFuture(deleteUserFormRoomSql, Seq(user.username))
    ) yield ()).closeLastConnection
  }

  override def listPublicRooms(): Future[Seq[Room]] = {
    log.debug("listPublicRooms()")
    (for (
      _ <- checkInitialization(notInitialized);
      conn <- openConnection();
      roomsResult <- conn.queryFuture(selectAllPublicRoomsSql)
    ) yield RoomsDatabaseResultsManager.resultOfJoinToRooms(roomsResult)).closeLastConnection
  }

  override def enterPublicRoom(playersNumber: Int)
                              (implicit user: Participant, address: Address): Future[Unit] = {
    log.debug(s"enterPublicRoom() playersNumber:$playersNumber, user:$user, address:$address")
    for (
      _ <- checkInitialization(notInitialized);
      roomID <- publicRoomIdFromPlayersNumber(playersNumber);
      _ <- enterRoom(roomID)(user, address)
    ) yield ()
  }

  override def publicRoomInfo(playersNumber: Int): Future[(Room, Seq[Address])] = {
    log.debug(s"publicRoomInfo() playersNumber:$playersNumber")
    for (
      _ <- checkInitialization(notInitialized);
      roomID <- publicRoomIdFromPlayersNumber(playersNumber);
      info <- roomInfo(roomID)
    ) yield info
  }

  override def exitPublicRoom(playersNumber: Int)
                             (implicit user: User): Future[Unit] = {
    log.debug(s"exitPublicRoom() playersNumber:$playersNumber, user:$user")
    for (
      _ <- checkInitialization(notInitialized);
      roomID <- publicRoomIdFromPlayersNumber(playersNumber);
      _ <- exitRoom(roomID)
    ) yield ()
  }

  override def deleteRoom(roomID: String): Future[Unit] = {

    def giveErrorOnNonFullRoom(roomID: String, connection: SQLConnection): Future[Unit] =
      checkRoomSpaceAvailable(roomID, connection, "") // if no room space available, will give empty error message
        // if no error received, should fail -> because room non full
        .flatMap(_ => Future.failed(new IllegalStateException(DELETING_NON_FULL_ROOM_ERROR)))
        // if first error reaches this point, room is full and can be deleted
        .recoverWith({ case ex: IllegalStateException if ex.getMessage.isEmpty => Future.successful(()) })

    log.debug(s"deleteRoom() roomID:$roomID")
    (for (
      _ <- checkInitialization(notInitialized);
      room <- roomInfo(roomID).map(_._1);
      conn <- openConnection();
      _ <- giveErrorOnNonFullRoom(roomID, conn);

      // outer for will reach this point only if room is full (and can be deleted)
      userDeletionFutures = for (user <- room.participants;
                                 deletionFuture = conn.updateWithParamsFuture(deleteUserFormRoomSql, Seq(user.username))) yield deletionFuture;
      _ <- Future.sequence(userDeletionFutures); // waits for all deletions to complete
      _ <- conn.updateWithParamsFuture(deleteRoomSql, Seq(roomID))
    ) yield ()).closeLastConnection
  }

  override def deleteAndRecreatePublicRoom(playersNumber: Int): Future[Unit] = {
    log.debug(s"deleteAndRecreatePublicRoom() playersNumber:$playersNumber")
    (for (
      _ <- checkInitialization(notInitialized);
      roomID <- publicRoomIdFromPlayersNumber(playersNumber);
      _ <- deleteRoom(roomID);
      conn <- openConnection();
      _ <- createPublicRoom(conn, playersNumber)
    ) yield ()).closeLastConnection
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

  private val ROOM_ID_LENGTH = 20
  private val userToRoomLinkField = "user_room"
  private val createRoomTableSql =
    s"""
        CREATE TABLE IF NOT EXISTS room (
          ${Room.FIELD_IDENTIFIER} VARCHAR(100) NOT NULL,
          ${Room.FIELD_NAME} VARCHAR(50) NOT NULL,
          ${Room.FIELD_NEEDED_PLAYERS} INT NOT NULL,
          PRIMARY KEY (${Room.FIELD_IDENTIFIER})
        )
      """
  private val createUserTableSql =
    s"""
        CREATE TABLE IF NOT EXISTS user (
          ${User.FIELD_USERNAME} VARCHAR(50) NOT NULL,
          ${Address.FIELD_ADDRESS} VARCHAR(255) NOT NULL,
          ${Address.FIELD_ADDRESS}2 VARCHAR(255) NOT NULL,
          $userToRoomLinkField VARCHAR(100),
          PRIMARY KEY (${User.FIELD_USERNAME}),
          CONSTRAINT FK_userRooms FOREIGN KEY ($userToRoomLinkField) REFERENCES room(${Room.FIELD_IDENTIFIER})
        )
      """

  private val insertNewRoomSql = "INSERT INTO room VALUES (?, ?, ?)"
  private val insertUserInRoomSql = "INSERT INTO user VALUES (?, ?, ?, ?)"
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
                                        (implicit executionContext: ExecutionContext): Future[String] = {

    // scalastyle:off method.name
    def _getNotAlreadyPresentRoomID(toCheckID: String, connection: SQLConnection): Future[String] =
    // scalastyle:on method.name
      checkRoomPresence(toCheckID, connection, "")
        .flatMap(_ => _getNotAlreadyPresentRoomID(generator, connection))
        .fallbackTo(Future.successful(toCheckID))

    _getNotAlreadyPresentRoomID(generator, connection)
  }

  /**
    * @return the future containing the room where optionally the user is
    */
  private def getRoomOfUser(connection: SQLConnection, user: User)
                           (implicit executionContext: ExecutionContext) = {
    connection.queryWithParamsFuture(selectRoomByUserSql, Seq(user.username))
      .map(RoomsDatabaseResultsManager.resultOfJoinToRooms(_).headOption)
  }

  /**
    * Method that generates a random room identifier
    */
  private def generateRandomRoomID() = Utils.randomString(ROOM_ID_LENGTH)

  /**
    * Utility method to check if DAO is initialized
    */
  private def checkInitialization(notInitialized: Boolean): Future[Unit] = {
    if (notInitialized) Future.failed(new IllegalStateException("Not initialized, you should first call initialize()"))
    else Future.successful(())
  }

  /**
    * @return a succeeded Future if string is ok, a failed Future otherwise
    */
  private def stringCheckFuture(toCheck: String, errorMessage: String): Future[Unit] =
    Future {
      require(!emptyString(toCheck), errorMessage)
    }(ExecutionContext.Implicits.global)

  /**
    * @return a succeeded Future if playersNumber is correct, a failed Future otherwise
    */
  private def playersNumberCheck(playersNumber: Int, errorMessage: String): Future[Unit] =
    if (playersNumber < 2) Future.failed(new IllegalArgumentException(errorMessage))
    else Future.successful(())

  /**
    * @return a succeeded future if the room with given ID is present, a failed future otherwise
    */
  private def checkRoomPresence(roomID: String, connection: SQLConnection, errorMessage: String)
                               (implicit executionContext: ExecutionContext): Future[Unit] = {
    connection.queryWithParamsFuture(selectARoomIDSql, Seq(roomID))
      .flatMap(_.getResults.size match {
        case 0 => Future.failed(new NoSuchElementException(errorMessage))
        case _ => Future.successful(())
      })
  }

  /**
    * @return a succeeded future if the room has available space for other users
    */
  private def checkRoomSpaceAvailable(roomID: String, connection: SQLConnection, errorMessage: String)
                                     (implicit executionContext: ExecutionContext): Future[Unit] = {
    connection.queryWithParamsFuture(selectRoomByIDSql, Seq(roomID))
      .map(RoomsDatabaseResultsManager.resultOfJoinToRooms)
      .flatMap(_.head match {
        case Room(_, _, playersNumber, actualParticipants)
          if playersNumber > actualParticipants.size => Future.successful(())
        case _ => Future.failed(new IllegalStateException(errorMessage))
      })
  }

  /**
    * Internal method to create public rooms
    *
    * @return the future that completes when the room is created
    */
  private def createPublicRoom(connection: SQLConnection, playersNumber: Int)
                              (implicit executionContext: ExecutionContext): Future[Unit] =
    for (
      publicRoomID <- getNotAlreadyPresentRoomID(connection, s"$publicPrefix${generateRandomRoomID()}");
      _ <- connection.updateWithParamsFuture(insertNewRoomSql, Seq(publicRoomID, s"$publicPrefix$playersNumber", playersNumber.toString))
    ) yield ()


  /**
    * An object to contain management of results from database
    *
    * @author Enrico Siboni
    */
  private object RoomsDatabaseResultsManager {

    private val ROOM_ID_JOIN_POSITION = 0
    private val ROOM_NAME_JOIN_POSITION = 1
    private val ROOM_PLAYERS_JOIN_POSITION = 2
    private val USER_USERNAME_JOIN_POSITION = 3
    private val USER_ADDRESS_JOIN_POSITION = 4
    private val USER_NOTIFICATION_ADDRESS_JOIN_POSITION = 5

    /**
      * Utility method to convert a result set to a room sequence and relative addresses
      */
    def resultOfJoinToRoomsWithAddresses(resultSet: ResultSet): Seq[(Room, Seq[Address])] = {
      val rooms = resultOfJoinToRooms(resultSet)
      val roomsAddresses = roomsAndNotificationAddresses(resultSet, ROOM_ID_JOIN_POSITION, USER_NOTIFICATION_ADDRESS_JOIN_POSITION)

      for (
        room <- rooms;
        roomAddresses <- roomsAddresses if room.identifier == roomAddresses._1
      ) yield (room, roomAddresses._2)
    }

    /**
      * Utility method to convert a result set to a room sequence
      */
    def resultOfJoinToRooms(resultSet: ResultSet): Seq[Room] = {
      val roomsUsers = roomsAndUsersInside(resultSet, ROOM_ID_JOIN_POSITION, USER_USERNAME_JOIN_POSITION, USER_ADDRESS_JOIN_POSITION)
      val roomsInfo = roomsAndInformation(resultSet, ROOM_ID_JOIN_POSITION, ROOM_NAME_JOIN_POSITION, ROOM_PLAYERS_JOIN_POSITION)

      (for (
        roomUsers <- roomsUsers;
        roomInfo <- roomsInfo if roomUsers._1 == roomInfo._1
      ) yield Room(roomInfo._1, roomInfo._2._1, roomInfo._2._2, roomUsers._2)).toSeq
    }

    /**
      * Extracts a map of roomIds to Room participant list from the resultSet passed in
      *
      * @param resultSet      the result set to use extracting information
      * @param roomIDPos      the position of roomID in result rows
      * @param userNamePos    the position of userName in result rows
      * @param userAddressPos the position of userAddress in result rows
      * @return the map that has roomId as key and room participants as value
      */
    private def roomsAndUsersInside(resultSet: ResultSet,
                                    roomIDPos: Int,
                                    userNamePos: Int,
                                    userAddressPos: Int): mutable.Map[String, Seq[Participant]] = {
      val roomsUsers: mutable.Map[String, Seq[Participant]] = mutable.HashMap()
      for (resultRow <- resultSet.getResults) {
        val roomID = resultRow getString roomIDPos
        val userName = if (resultRow hasNull userNamePos) None else Some(resultRow getString userNamePos)
        val userAddress = if (resultRow hasNull userAddressPos) None else Some(resultRow getString userAddressPos)

        if (roomsUsers contains roomID) {
          userName.foreach(name => roomsUsers(roomID) = roomsUsers(roomID) :+ Participant(name, userAddress.get))
        } else {
          (userName, userAddress) match {
            case (Some(name), Some(address)) => roomsUsers(roomID) = Seq(Participant(name, address))
            case _ => roomsUsers(roomID) = Seq()
          }
        }
      }
      roomsUsers
    }

    /**
      * Extracts a map of roomId to player notification addresses list from resultSet passed in
      *
      * @param resultSet                  the result set to extract information
      * @param roomIDPos                  the roomID Position in result rows
      * @param userNotificationAddressPos the user notification address in result row
      * @return the map that has roomId as key and list of notification addresses as value
      */
    private def roomsAndNotificationAddresses(resultSet: ResultSet,
                                              roomIDPos: Int,
                                              userNotificationAddressPos: Int): mutable.Map[String, Seq[Address]] = {
      val roomsAddresses: mutable.Map[String, Seq[Address]] = mutable.HashMap()
      for (resultRow <- resultSet.getResults) {
        val roomID = resultRow getString roomIDPos
        val userNotificationAddress = if (resultRow hasNull userNotificationAddressPos) None else Some(resultRow getString userNotificationAddressPos)

        if (roomsAddresses contains roomID) {
          userNotificationAddress.foreach(notificationAddress => roomsAddresses(roomID) = roomsAddresses(roomID) :+ Address(notificationAddress))
        } else {
          userNotificationAddress match {
            case Some(notificationAddress) => roomsAddresses(roomID) = Seq(Address(notificationAddress))
            case None => roomsAddresses(roomID) = Seq()
          }
        }
      }
      roomsAddresses
    }

    /**
      * Extracts a map of roomId to room Information from resultSet passed in
      *
      * @param resultSet      the result set to use extracting information
      * @param roomIDPos      the roomID Position in result rows
      * @param roomNamePos    the roomName position in result row
      * @param roomPlayersPos the room players position in result row
      * @return the map that has roomId as key and room information as value
      */
    private def roomsAndInformation(resultSet: ResultSet,
                                    roomIDPos: Int,
                                    roomNamePos: Int,
                                    roomPlayersPos: Int): mutable.Map[String, (String, Int)] = {
      val roomsInfo: mutable.Map[String, (String, Int)] = mutable.HashMap()
      for (resultRow <- resultSet.getResults) {
        val roomID = resultRow getString roomIDPos
        if (!(roomsInfo contains roomID)) {
          val roomName = resultRow getString roomNamePos
          val roomNeededPlayers = resultRow getInteger roomPlayersPos
          roomsInfo(roomID) = (roomName, roomNeededPlayers)
        }
      }
      roomsInfo
    }
  }

}
