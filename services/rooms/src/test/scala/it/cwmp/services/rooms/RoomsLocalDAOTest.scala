package it.cwmp.services.rooms

import it.cwmp.model.{Address, Participant, User}
import it.cwmp.testing.FutureMatchers
import it.cwmp.testing.rooms.RoomsTesting
import it.cwmp.utils.Utils
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.Future

/**
  * Test class for local RoomsDAO
  *
  * @author Enrico Siboni
  */
class RoomsLocalDAOTest extends RoomsTesting with BeforeAndAfterEach with FutureMatchers {

  private var daoFuture: Future[RoomDAO] = _

  private val userAddress = "http://127.0.1.1:8668/api/client/pFU9qOCU3kmYqwk1qqkl/room/participants"
  private implicit val user: Participant = Participant("Enrico", userAddress)
  private val notificationAddress: Address = Address("http://127.0.1.1:8668/api/client/pFU9qOCU3kmYqwk1qqkl/room/participants")

  override protected def beforeEach(): Unit = {
    val localDAO = RoomsLocalDAO()
    daoFuture = localDAO.initialize() map (_ => localDAO)
  }

  /**
    * @return a fresh random participant
    */
  def randomParticipant: Participant = Participant(Utils.randomString(user.username.length), userAddress)

  override protected def privateRoomCreationTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed returning roomId, if parameters are correct") {
      for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber)) yield roomID should not be empty
    }
    describe(shouldFail) {
      it("if roomName empty") {
        for (dao <- daoFuture; assertion <- dao.createRoom("", playersNumber).shouldFailWith[IllegalArgumentException])
          yield assertion
      }
      it("if playersNumber less than 2") {
        for (dao <- daoFuture; assertion <- dao.createRoom(roomName, 0).shouldFailWith[IllegalArgumentException])
          yield assertion
      }
    }
  }

  override protected def privateRoomEnteringTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomId is provided") {
      for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
           _ <- dao.enterRoom(roomID)(user, notificationAddress);
           _ <- cleanUpRoom(roomID)) yield succeed
    }
    it("user should be inside after it") {
      for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
           _ <- dao.enterRoom(roomID)(user, notificationAddress); roomInfo <- dao.roomInfo(roomID);
           assertion <- assert(roomInfo._1.participants.contains(user) && roomInfo._2.contains(notificationAddress));
           _ <- cleanUpRoom(roomID)) yield assertion
    }

    describe(shouldFail) {
      onWrongRoomID(roomID => daoFuture flatMap (_.enterRoom(roomID)(user, notificationAddress)))

      it("if user is already inside a room") {
        for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
             _ <- dao.enterRoom(roomID)(user, notificationAddress);
             assertion <- dao.enterRoom(roomID)(user, notificationAddress).shouldFailWith[IllegalStateException];
             _ <- cleanUpRoom(roomID)) yield assertion
      }
      it("if the room is full") {
        val playersNumber = 2
        for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
             _ <- dao.enterRoom(roomID)(randomParticipant, notificationAddress);
             _ <- dao.enterRoom(roomID)(randomParticipant, notificationAddress);
             assertion <- dao.enterRoom(roomID)(randomParticipant, notificationAddress).shouldFailWith[IllegalStateException])
          yield assertion
      }
    }
  }

  override protected def privateRoomInfoRetrievalTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomId is correct") {
      for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
           roomInfo <- dao.roomInfo(roomID)) yield roomInfo._1.participants shouldBe empty
    }

    it("should succeed and contain entered users") {
      for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
           _ <- dao.enterRoom(roomID)(user, notificationAddress);
           roomInfo <- dao.roomInfo(roomID);
           assertion <- assert(roomInfo._1.participants.contains(user) && roomInfo._2.contains(notificationAddress));
           _ <- cleanUpRoom(roomID)) yield assertion
    }

    describe(shouldFail) {
      onWrongRoomID(roomID => daoFuture flatMap (_.roomInfo(roomID)))
    }
  }

  override protected def privateRoomExitingTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomID is correct and user inside") {
      for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
           _ <- dao.enterRoom(roomID)(user, notificationAddress);
           _ <- dao.exitRoom(roomID)) yield succeed
    }
    it("user should not be inside after it") {
      for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
           _ <- dao.enterRoom(roomID)(user, notificationAddress);
           _ <- dao.exitRoom(roomID);
           roomInfo <- dao.roomInfo(roomID)) yield roomInfo._1.participants shouldNot contain(user)
    }

    describe(shouldFail) {
      onWrongRoomID(roomID => daoFuture flatMap (_.exitRoom(roomID)))

      it("if user is not inside the room") {
        for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
             assertion <- dao.exitRoom(roomID).shouldFailWith[IllegalStateException]) yield assertion
      }
      it("if user is inside another room") {
        for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
             otherRoomID <- dao.createRoom(roomName, playersNumber);
             _ <- dao.enterRoom(roomID)(user, notificationAddress);
             assertion <- dao.exitRoom(otherRoomID).shouldFailWith[IllegalStateException];
             _ <- cleanUpRoom(roomID)) yield assertion
      }
    }
  }

  override protected def publicRoomEnteringTests(playersNumber: Int): Unit = {
    describe("should succeed") {
      it("if room with such players number exists") {
        for (dao <- daoFuture;
             _ <- dao.enterPublicRoom(playersNumber)(user, notificationAddress);
             _ <- cleanUpRoom(playersNumber)) yield succeed
      }
      it("and the user should be inside it") {
        for (dao <- daoFuture; _ <- dao.enterPublicRoom(playersNumber)(user, notificationAddress);
             roomInfo <- dao.publicRoomInfo(playersNumber);
             assertion <- assert(roomInfo._1.participants.contains(user) && roomInfo._2.contains(notificationAddress));
             _ <- cleanUpRoom(playersNumber)) yield assertion
      }
    }

    describe(shouldFail) {
      onWrongPlayersNumber(playersNumber => daoFuture flatMap (_.enterPublicRoom(playersNumber)(user, notificationAddress)))

      it("if user is already inside a room") {
        for (dao <- daoFuture; _ <- dao.enterPublicRoom(2)(user, notificationAddress);
             assertion <- dao.enterPublicRoom(3)(user, notificationAddress).shouldFailWith[IllegalStateException];
             _ <- cleanUpRoom(playersNumber)) yield assertion
      }
      it("if room is full") {
        val firstParticipant = randomParticipant
        val secondParticipant = randomParticipant
        for (dao <- daoFuture; _ <- dao.enterPublicRoom(2)(firstParticipant, notificationAddress);
             _ <- dao.enterPublicRoom(2)(secondParticipant, notificationAddress);
             assertion <- dao.enterPublicRoom(2)(randomParticipant, notificationAddress).shouldFailWith[IllegalStateException];
             _ <- cleanUpRoom(playersNumber)(firstParticipant);
             _ <- cleanUpRoom(playersNumber)(secondParticipant)) yield assertion
      }
    }
  }

  override protected def publicRoomListingTests(playersNumber: Int): Unit = {
    it("should be nonEmpty") {
      for (dao <- daoFuture; rooms <- dao.listPublicRooms()) yield rooms should not be empty
    }
    it("should show only public rooms") {
      for (dao <- daoFuture; _ <- dao.createRoom("TestRoom", 2);
           rooms <- dao.listPublicRooms())
        yield rooms.forall(_.identifier.contains(RoomsLocalDAO.publicPrefix)) shouldBe true
    }
  }

  override protected def publicRoomInfoRetrievalTests(playersNumber: Int): Unit = {
    it("should succeed if provided playersNumber is correct") {
      for (dao <- daoFuture; roomInfo <- dao.publicRoomInfo(playersNumber))
        yield roomInfo._1.participants shouldBe empty
    }
    it("should show entered players") {
      for (dao <- daoFuture; _ <- dao.enterPublicRoom(playersNumber)(user, notificationAddress);
           roomInfo <- dao.publicRoomInfo(playersNumber);
           assertion <- assert(roomInfo._1.participants.contains(user) && roomInfo._2.contains(notificationAddress));
           _ <- cleanUpRoom(playersNumber)) yield assertion
    }

    describe(shouldFail) {
      onWrongPlayersNumber(playersNumber => daoFuture flatMap (_.publicRoomInfo(playersNumber)))
    }
  }

  override protected def publicRoomExitingTests(playersNumber: Int): Unit = {
    it("should succeed if players number is correct and user is inside") {
      for (dao <- daoFuture; _ <- dao.enterPublicRoom(playersNumber)(user, notificationAddress);
           _ <- dao.exitPublicRoom(playersNumber)) yield succeed
    }
    it("user should not be inside after it") {
      for (dao <- daoFuture; _ <- dao.enterPublicRoom(playersNumber)(user, notificationAddress);
           _ <- dao.exitPublicRoom(playersNumber);
           roomInfo <- dao.publicRoomInfo(playersNumber)) yield roomInfo._1.participants shouldNot contain(user)
    }

    describe(shouldFail) {
      onWrongPlayersNumber(playersNumber => daoFuture flatMap (_.exitPublicRoom(playersNumber)))

      it("if user is not inside the room") {
        for (dao <- daoFuture; assertion <- dao.exitPublicRoom(playersNumber).shouldFailWith[IllegalStateException])
          yield assertion
      }
      it("if user is inside another room") {
        for (dao <- daoFuture; _ <- dao.enterPublicRoom(playersNumber)(user, notificationAddress);
             assertion <- dao.exitPublicRoom(playersNumber + 1).shouldFailWith[IllegalStateException];
             _ <- cleanUpRoom(playersNumber)) yield assertion
      }
    }
  }

  describe("Deletion") {
    describe("of Private Room") {
      val roomName = "Stanza"
      val playersNumber = 2

      it("should succeed if room is full") {
        for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
             _ <- dao.enterRoom(roomID)(randomParticipant, notificationAddress);
             _ <- dao.enterRoom(roomID)(randomParticipant, notificationAddress);
             _ <- dao.deleteRoom(roomID)) yield succeed
      }
      it("should succeed removing the room") {
        for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
             _ <- dao.enterRoom(roomID)(randomParticipant, notificationAddress);
             _ <- dao.enterRoom(roomID)(randomParticipant, notificationAddress);
             _ <- dao.deleteRoom(roomID);
             assertion <- dao.roomInfo(roomID).shouldFailWith[NoSuchElementException]) yield assertion
      }

      describe(shouldFail) {
        onWrongRoomID(roomID => daoFuture flatMap (_.deleteRoom(roomID)))

        it("if room is not full") {
          for (dao <- daoFuture; roomID <- dao.createRoom(roomName, playersNumber);
               _ <- dao.enterRoom(roomID)(user, notificationAddress);
               assertion <- dao.deleteRoom(roomID).shouldFailWith[IllegalStateException];
               _ <- cleanUpRoom(roomID)) yield assertion
        }
      }
    }

    describe("of Public Room") {
      val playersNumber = 2

      it("should succeed if room is full, and recreate an empty public room with same players number") {
        for (dao <- daoFuture; _ <- dao.enterPublicRoom(playersNumber)(randomParticipant, notificationAddress);
             _ <- dao.enterPublicRoom(playersNumber)(randomParticipant, notificationAddress);
             _ <- dao.deleteAndRecreatePublicRoom(playersNumber);
             roomInfo <- dao.publicRoomInfo(playersNumber)) yield roomInfo._1.participants shouldBe empty
      }

      describe(shouldFail) {
        onWrongPlayersNumber(playersNumber => daoFuture flatMap (_.deleteAndRecreatePublicRoom(playersNumber)))

        it("if room is not full") {
          for (dao <- daoFuture; _ <- dao.enterPublicRoom(playersNumber)(user, notificationAddress);
               assertion <- dao.deleteAndRecreatePublicRoom(playersNumber).shouldFailWith[IllegalStateException];
               _ <- cleanUpRoom(playersNumber)) yield assertion
        }
      }
    }
  }


  override protected def onWrongRoomID(test: String => Future[_]): Unit = {
    it("if roomId is empty") {
      test("").shouldFailWith[IllegalArgumentException]
    }
    it("if provided roomId is not present") {
      test("11111111").shouldFailWith[NoSuchElementException]
    }
  }

  override protected def onWrongPlayersNumber(test: Int => Future[_]): Unit = {
    it("if provided players number is less than 2") {
      test(0).shouldFailWith[IllegalArgumentException]
    }
    it("if there's no room with such players number") {
      test(TOO_BIG_PLAYERS_NUMBER).shouldFailWith[NoSuchElementException]
    }
  }

  describe("The Helper shouldn't work") {
    describe("if not initialized") {
      val fakeRoomName = "Stanza"
      val fakeRoomID = "12342134"
      val fakePlayersNumber = 2

      it("createRoom") {
        RoomsLocalDAO().createRoom(fakeRoomName, fakePlayersNumber).shouldFailWith[IllegalStateException]
      }
      it("enterRoom") {
        RoomsLocalDAO().enterRoom(fakeRoomID).shouldFailWith[IllegalStateException]
      }
      it("roomInfo") {
        RoomsLocalDAO().roomInfo(fakeRoomID).shouldFailWith[IllegalStateException]
      }
      it("exitRoom") {
        RoomsLocalDAO().exitRoom(fakeRoomID).shouldFailWith[IllegalStateException]
      }
      it("listPublicRooms") {
        RoomsLocalDAO().listPublicRooms().shouldFailWith[IllegalStateException]
      }
      it("enterPublicRoom") {
        RoomsLocalDAO().enterPublicRoom(fakePlayersNumber).shouldFailWith[IllegalStateException]
      }
      it("publicRoomInfo") {
        RoomsLocalDAO().publicRoomInfo(fakePlayersNumber).shouldFailWith[IllegalStateException]
      }
      it("exitPublicRoom") {
        RoomsLocalDAO().exitPublicRoom(fakePlayersNumber).shouldFailWith[IllegalStateException]
      }
      it("deleteRoom") {
        RoomsLocalDAO().deleteRoom(fakeRoomID).shouldFailWith[IllegalStateException]
      }
      it("deleteAndRecreatePublicRoom") {
        RoomsLocalDAO().deleteAndRecreatePublicRoom(fakePlayersNumber).shouldFailWith[IllegalStateException]
      }
    }
  }

  /**
    * Cleans up the provided private room, exiting provided player
    */
  private def cleanUpRoom(roomID: String)(implicit user: User) =
    for (dao <- daoFuture; _ <- dao.exitRoom(roomID)(user)) yield ()

  /**
    * Cleans up the provided public room, exiting provided user
    */
  private def cleanUpRoom(playersNumber: Int)(implicit user: User) =
    for (dao <- daoFuture; _ <- dao.exitPublicRoom(playersNumber)(user)) yield ()
}
