package it.cwmp.services.rooms

import io.vertx.core.json.JsonObject
import io.vertx.scala.ext.jdbc.JDBCClient
import it.cwmp.model.{Address, Participant}
import it.cwmp.testing.FutureMatchers
import it.cwmp.testing.rooms.RoomsTesting
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
    daoFuture = createRoomLocalDAO flatMap (dao => dao.initialize() map (_ => dao))
  }

  override protected def privateRoomCreationTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed returning roomId, if parameters are correct") {
      daoFuture.flatMap(_.createRoom(roomName, playersNumber))
        .flatMap(_ should not be empty)
    }
    describe("should fail") {
      it("if roomName empty") {
        daoFuture.flatMap(_.createRoom("", playersNumber)).shouldFailWith[IllegalArgumentException]
      }
      it("if playersNumber less than 2") {
        daoFuture.flatMap(_.createRoom(roomName, 0)).shouldFailWith[IllegalArgumentException]
      }
    }
  }

  override protected def privateRoomEnteringTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomId is provided") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(dao.enterRoom(_)(user, notificationAddress))) flatMap (_ => succeed)
    }
    it("user should be inside after it") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(roomID => (dao.enterRoom(roomID)(user, notificationAddress) flatMap (_ => dao.roomInfo(roomID)))
          .flatMap(roomInfo => assert(roomInfo._1.participants.contains(user) && roomInfo._2.contains(notificationAddress)))))
    }

    describe("should fail") {
      onWrongRoomID(roomID => daoFuture.flatMap(_.enterRoom(roomID)(user, notificationAddress)))

      it("if user is already inside a room") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.enterRoom(roomID)(user, notificationAddress)
            .flatMap(_ => dao.enterRoom(roomID)(user, notificationAddress))))
          .shouldFailWith[IllegalStateException]
      }
      it("if the room is full") {
        val playersNumber = 2
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.enterRoom(roomID)(Participant("User1", userAddress), notificationAddress)
            .flatMap(_ => dao.enterRoom(roomID)(Participant("User2", userAddress), notificationAddress))
            .flatMap(_ => dao.enterRoom(roomID)(Participant("User3", userAddress), notificationAddress))
          ))
          .shouldFailWith[IllegalStateException]
      }
    }
  }

  override protected def privateRoomInfoRetrievalTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomId is correct") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(dao.roomInfo)) flatMap (_._1.participants shouldBe empty)
    }

    it("should succeed and contain entered users") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(roomID => dao.enterRoom(roomID)(user, notificationAddress)
          .flatMap(_ => dao.roomInfo(roomID))))
        .flatMap(roomInfo => assert(roomInfo._1.participants.contains(user) && roomInfo._2.contains(notificationAddress)))
    }

    describe("should fail") {
      onWrongRoomID(roomID => daoFuture.flatMap(_.roomInfo(roomID)))
    }
  }

  override protected def privateRoomExitingTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomID is correct and user inside") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(roomID => dao.enterRoom(roomID)(user, notificationAddress) flatMap (_ => dao.exitRoom(roomID))))
        .map(_ => succeed)
    }
    it("user should not be inside after it") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(roomID => dao.enterRoom(roomID)(user, notificationAddress)
          .flatMap(_ => dao.exitRoom(roomID)) flatMap (_ => dao.roomInfo(roomID))))
        .flatMap(_._1.participants shouldNot contain(user))
    }

    describe("should fail") {
      onWrongRoomID(roomID => daoFuture.flatMap(_.exitRoom(roomID)))

      it("if user is not inside the room") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber) flatMap dao.exitRoom)
          .shouldFailWith[IllegalStateException]
      }
      it("if user is inside another room") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.createRoom(roomName, playersNumber)
            .flatMap(otherRoomID => dao.enterRoom(otherRoomID)(user, notificationAddress)) flatMap (_ => dao.exitRoom(roomID))))
          .shouldFailWith[IllegalStateException]
      }
    }
  }

  override protected def publicRoomEnteringTests(playersNumber: Int): Unit = {
    describe("should succeed") {
      it("if room with such players number exists") {
        daoFuture.flatMap(_.enterPublicRoom(playersNumber)(user, notificationAddress)) map (_ => succeed)
      }
      it("and the user should be inside it") {
        daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber)(user, notificationAddress) flatMap (_ => dao.publicRoomInfo(playersNumber)))
          .flatMap(roomInfo => assert(roomInfo._1.participants.contains(user) && roomInfo._2.contains(notificationAddress)))
      }
    }

    describe("should fail") {
      onWrongPlayersNumber(playersNumber => daoFuture.flatMap(_.enterPublicRoom(playersNumber)(user, notificationAddress)))

      it("if user is already inside a room") {
        daoFuture.flatMap(dao => dao.enterPublicRoom(2)(user, notificationAddress)
          .flatMap(_ => dao.enterPublicRoom(3)(user, notificationAddress)))
          .shouldFailWith[IllegalStateException]
      }
      it("if room is full") {
        daoFuture.flatMap(dao => dao.enterPublicRoom(2)(Participant("User1", userAddress), notificationAddress)
          .flatMap(_ => dao.enterPublicRoom(2)(Participant("User2", userAddress), notificationAddress))
          .flatMap(_ => dao.enterPublicRoom(2)(Participant("User3", userAddress), notificationAddress)))
          .shouldFailWith[IllegalStateException]
      }
    }
  }

  override protected def publicRoomListingTests(playersNumber: Int): Unit = {
    it("should be nonEmpty") {
      daoFuture flatMap (_.listPublicRooms()) flatMap (_ should not be empty)
    }
    it("should show only public rooms") {
      daoFuture.flatMap(dao => dao.createRoom("TestRoom", 2)
        .flatMap(_ => dao.listPublicRooms()))
        .flatMap(_.forall(_.identifier.contains(RoomsLocalDAO.publicPrefix)) shouldBe true)
    }
  }

  override protected def publicRoomInfoRetrievalTests(playersNumber: Int): Unit = {
    it("should succeed if provided playersNumber is correct") {
      daoFuture.flatMap(_.publicRoomInfo(playersNumber)) flatMap (_._1.participants shouldBe empty)
    }
    it("should show entered players") {
      daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber)(user, notificationAddress) flatMap (_ => dao.publicRoomInfo(playersNumber)))
        .flatMap(roomInfo => assert(roomInfo._1.participants.contains(user) && roomInfo._2.contains(notificationAddress)))
    }

    describe("should fail") {
      onWrongPlayersNumber(playersNumber => daoFuture.flatMap(_.publicRoomInfo(playersNumber)))
    }
  }

  override protected def publicRoomExitingTests(playersNumber: Int): Unit = {
    it("should succeed if players number is correct and user is inside") {
      daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber)(user, notificationAddress) flatMap (_ => dao.exitPublicRoom(playersNumber)))
        .flatMap(_ => succeed)
    }
    it("user should not be inside after it") {
      daoFuture.flatMap(dao => (dao.enterPublicRoom(playersNumber)(user, notificationAddress) flatMap (_ => dao.exitPublicRoom(playersNumber)))
        .flatMap(_ => dao.publicRoomInfo(playersNumber))) flatMap (_._1.participants shouldNot contain(user))
    }

    describe("should fail") {
      onWrongPlayersNumber(playersNumber => daoFuture.flatMap(_.exitPublicRoom(playersNumber)))

      it("if user is not inside the room") {
        daoFuture.flatMap(_.exitPublicRoom(playersNumber)).shouldFailWith[IllegalStateException]
      }
      it("if user is inside another room") {
        daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber)(user, notificationAddress) flatMap (_ => dao.exitPublicRoom(playersNumber + 1)))
          .shouldFailWith[IllegalStateException]
      }
    }
  }

  describe("Deletion") {
    describe("of Private Room") {
      val roomName = "Stanza"
      val playersNumber = 2

      it("should succeed if room is full") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.enterRoom(roomID)(Participant("User1", userAddress), notificationAddress)
            .flatMap(_ => dao.enterRoom(roomID)(Participant("User2", userAddress), notificationAddress))
            .flatMap(_ => dao.deleteRoom(roomID)))) map (_ => succeed)
      }
      it("should succeed removing the room") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.enterRoom(roomID)(Participant("User1", userAddress), notificationAddress)
            .flatMap(_ => dao.enterRoom(roomID)(Participant("User2", userAddress), notificationAddress))
            .flatMap(_ => dao.deleteRoom(roomID)) flatMap (_ => dao.roomInfo(roomID))))
          .shouldFailWith[NoSuchElementException]
      }

      describe("should fail") {
        onWrongRoomID(roomID => daoFuture.flatMap(_.deleteRoom(roomID)))

        it("if room is not full") {
          daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
            .flatMap(roomID => dao.enterRoom(roomID)(user, notificationAddress) flatMap (_ => dao.deleteRoom(roomID))))
            .shouldFailWith[IllegalStateException]
        }
      }
    }

    describe("of Public Room") {
      val playersNumber = 2

      it("should succeed if room is full, and recreate an empty public room with same players number") {
        daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber)(Participant("User1", userAddress), notificationAddress)
          .flatMap(_ => dao.enterPublicRoom(playersNumber)(Participant("User2", userAddress), notificationAddress))
          .flatMap(_ => dao.deleteAndRecreatePublicRoom(playersNumber))
          .flatMap(_ => dao.publicRoomInfo(playersNumber))) map (_._1.participants shouldBe empty)
      }

      describe("should fail") {
        onWrongPlayersNumber(playersNumber => daoFuture.flatMap(_.deleteAndRecreatePublicRoom(playersNumber)))

        it("if room is not full") {
          daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber)(user, notificationAddress) flatMap (_ => dao.deleteAndRecreatePublicRoom(playersNumber)))
            .shouldFailWith[IllegalStateException]
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
        createRoomLocalDAO.flatMap(_.createRoom(fakeRoomName, fakePlayersNumber)).shouldFailWith[IllegalStateException]
      }
      it("enterRoom") {
        createRoomLocalDAO.flatMap(_.enterRoom(fakeRoomID)).shouldFailWith[IllegalStateException]
      }
      it("roomInfo") {
        createRoomLocalDAO.flatMap(_.roomInfo(fakeRoomID)).shouldFailWith[IllegalStateException]
      }
      it("exitRoom") {
        createRoomLocalDAO.flatMap(_.exitRoom(fakeRoomID)).shouldFailWith[IllegalStateException]
      }
      it("listPublicRooms") {
        createRoomLocalDAO.flatMap(_.listPublicRooms()).shouldFailWith[IllegalStateException]
      }
      it("enterPublicRoom") {
        createRoomLocalDAO.flatMap(_.enterPublicRoom(fakePlayersNumber)).shouldFailWith[IllegalStateException]
      }
      it("publicRoomInfo") {
        createRoomLocalDAO.flatMap(_.publicRoomInfo(fakePlayersNumber)).shouldFailWith[IllegalStateException]
      }
      it("exitPublicRoom") {
        createRoomLocalDAO.flatMap(_.exitPublicRoom(fakePlayersNumber)).shouldFailWith[IllegalStateException]
      }
      it("deleteRoom") {
        createRoomLocalDAO.flatMap(_.deleteRoom(fakeRoomID)).shouldFailWith[IllegalStateException]
      }
      it("deleteAndRecreatePublicRoom") {
        createRoomLocalDAO.flatMap(_.deleteAndRecreatePublicRoom(fakePlayersNumber)).shouldFailWith[IllegalStateException]
      }
    }
  }

  /**
    * @return the created RoomsLocalDAO to test
    */
  private def createRoomLocalDAO: Future[RoomsLocalDAO] =
    loadLocalDBConfig.map(JDBCClient.createShared(vertx, _))
      .flatMap(client => client.querySingleFuture("DROP SCHEMA PUBLIC CASCADE")
        .map(_ => RoomsLocalDAO()))

  /**
    * @return the Future containing local database configuration JSON
    */
  private def loadLocalDBConfig: Future[JsonObject] =
    vertx fileSystem() readFileFuture "database/jdbc_config.json" map (_.toJsonObject)
}
