package it.cwmp.controller.rooms

import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.scala.ext.jdbc.JDBCClient
import it.cwmp.model.{Address, Participant, User}
import it.cwmp.testing.server.rooms.RoomsTesting
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Test class for local RoomsDAO
  *
  * @author Enrico Siboni
  */
class RoomsLocalDAOTest extends RoomsTesting with BeforeAndAfterEach {

  private var daoFuture: Future[RoomDAO] = _

  private val userAddress = "fakeAddress"
  private implicit val user: Participant = Participant("Enrico", userAddress)

  override protected def beforeEach(): Unit = {
    daoFuture = vertx.fileSystem.readFileFuture("database/jdbc_config.json")
      .map(config => JDBCClient.createShared(vertx, new JsonObject(config)))
      .map(client => RoomsLocalDAO(client))
      .flatMap(storage => storage.initialize().map(_ => storage))
  }

  override protected def privateRoomCreationTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed returning roomId, if parameters are correct") {
      daoFuture.flatMap(_.createRoom(roomName, playersNumber))
        .flatMap(_ should not be empty)
    }
    describe("should fail") {
      it("if roomName empty") {
        recoverToSucceededIf[IllegalArgumentException](daoFuture.flatMap(_.createRoom("", playersNumber)))
      }
      it("if playersNumber less than 2") {
        recoverToSucceededIf[IllegalArgumentException](daoFuture.flatMap(_.createRoom(roomName, 0)))
      }
    }
  }

  override protected def privateRoomEnteringTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomId is provided") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(dao.enterRoom)) flatMap (_ => succeed)
    }
    it("user should be inside after it") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(roomID => (dao.enterRoom(roomID) flatMap (_ => dao.roomInfo(roomID)))
          .flatMap(_.participants should contain(user))))
    }

    describe("should fail") {
      onWrongRoomID(roomID => daoFuture.flatMap(_.enterRoom(roomID)))

      it("if user is already inside a room") {
        recoverToSucceededIf[IllegalStateException] {
          daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
            .flatMap(roomID => dao.enterRoom(roomID) flatMap (_ => dao.enterRoom(roomID))))
        }
      }
      it("if the room is full") {
        recoverToSucceededIf[IllegalStateException] {
          val playersNumber = 2
          daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
            .flatMap(roomID => dao.enterRoom(roomID)(Participant("User1", userAddress))
              .flatMap(_ => dao.enterRoom(roomID)(Participant("User2", userAddress)))
              .flatMap(_ => dao.enterRoom(roomID)(Participant("User3", userAddress)))
            ))
        }
      }
    }
  }

  override protected def privateRoomInfoRetrievalTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomId is correct") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(dao.roomInfo)) flatMap (_.participants shouldBe empty)
    }

    describe("should fail") {
      onWrongRoomID(roomID => daoFuture.flatMap(_.roomInfo(roomID)))
    }
  }

  override protected def privateRoomExitingTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomID is correct and user inside") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(roomID => dao.enterRoom(roomID) flatMap (_ => dao.exitRoom(roomID))))
        .map(_ => succeed)
    }
    it("user should not be inside after it") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(roomID => dao.enterRoom(roomID)
          .flatMap(_ => dao.exitRoom(roomID)) flatMap (_ => dao.roomInfo(roomID))))
        .flatMap(_.participants shouldNot contain(user))
    }

    describe("should fail") {
      onWrongRoomID(roomID => daoFuture.flatMap(_.exitRoom(roomID)))

      it("if user is not inside the room") {
        recoverToSucceededIf[IllegalStateException] {
          daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber) flatMap dao.exitRoom)
        }
      }
      it("if user is inside another room") {
        recoverToSucceededIf[IllegalStateException] {
          daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
            .flatMap(roomID => dao.createRoom(roomName, playersNumber)
              .flatMap(otherRoomID => dao.enterRoom(otherRoomID)) flatMap (_ => dao.exitRoom(roomID))))
        }
      }
    }
  }

  override protected def publicRoomEnteringTests(playersNumber: Int): Unit = {
    describe("should succeed") {
      it("if room with such players number exists") {
        daoFuture.flatMap(_.enterPublicRoom(playersNumber)) map (_ => succeed)
      }
      it("and the user should be inside it") {
        daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber) flatMap (_ => dao.publicRoomInfo(playersNumber)))
          .flatMap(_.participants should contain(user))
      }
    }

    describe("should fail") {
      onWrongPlayersNumber(playersNumber => daoFuture.flatMap(_.enterPublicRoom(playersNumber)))

      it("if user is already inside a room") {
        recoverToSucceededIf[IllegalStateException] {
          daoFuture.flatMap(dao => dao.enterPublicRoom(2) flatMap (_ => dao.enterPublicRoom(3)))
        }
      }
      it("if room is full") {
        recoverToSucceededIf[IllegalStateException] {
          daoFuture.flatMap(dao => dao.enterPublicRoom(2)(Participant("User1", userAddress))
            .flatMap(_ => dao.enterPublicRoom(2)(Participant("User2", userAddress)))
            .flatMap(_ => dao.enterPublicRoom(2)(Participant("User3", userAddress))))
        }
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
      daoFuture.flatMap(_.publicRoomInfo(playersNumber)) flatMap (_.participants shouldBe empty)
    }
    it("should show entered players") {
      daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber) flatMap (_ => dao.publicRoomInfo(playersNumber)))
        .flatMap(_.participants should contain(user))
    }

    describe("should fail") {
      onWrongPlayersNumber(playersNumber => daoFuture.flatMap(_.publicRoomInfo(playersNumber)))
    }
  }

  override protected def publicRoomExitingTests(playersNumber: Int): Unit = {
    it("should succeed if players number is correct and user is inside") {
      daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber) flatMap (_ => dao.exitPublicRoom(playersNumber)))
        .flatMap(_ => succeed)
    }
    it("user should not be inside after it") {
      daoFuture.flatMap(dao => (dao.enterPublicRoom(playersNumber) flatMap (_ => dao.exitPublicRoom(playersNumber)))
        .flatMap(_ => dao.publicRoomInfo(playersNumber))) flatMap (_.participants shouldNot contain(user))
    }

    describe("should fail") {
      onWrongPlayersNumber(playersNumber => daoFuture.flatMap(_.exitPublicRoom(playersNumber)))

      it("if user is not inside the room") {
        recoverToSucceededIf[IllegalStateException](daoFuture.flatMap(_.exitPublicRoom(playersNumber)))
      }
      it("if user is inside another room") {
        recoverToSucceededIf[IllegalStateException] {
          daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber) flatMap (_ => dao.exitPublicRoom(playersNumber + 1)))
        }
      }
    }
  }

  describe("Deletion") {
    describe("of Private Room") {
      val roomName = "Stanza"
      val playersNumber = 2

      it("should succeed if room is full") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.enterRoom(roomID)(Participant("User1", userAddress))
            .flatMap(_ => dao.enterRoom(roomID)(Participant("User2", userAddress)))
            .flatMap(_ => dao.deleteRoom(roomID)))) map (_ => succeed)
      }
      it("should succeed removing the room") {
        recoverToSucceededIf[NoSuchElementException] {
          daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
            .flatMap(roomID => dao.enterRoom(roomID)(Participant("User1", userAddress))
              .flatMap(_ => dao.enterRoom(roomID)(Participant("User2", userAddress)))
              .flatMap(_ => dao.deleteRoom(roomID)) flatMap (_ => dao.roomInfo(roomID))))
        }
      }

      describe("should fail") {
        onWrongRoomID(roomID => daoFuture.flatMap(_.deleteRoom(roomID)))

        it("if room is not full") {
          recoverToSucceededIf[IllegalStateException] {
            daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
              .flatMap(roomID => dao.enterRoom(roomID) flatMap (_ => dao.deleteRoom(roomID))))
          }
        }
      }
    }

    describe("of Public Room") {
      val playersNumber = 2

      it("should succeed if room is full, and recreate an empty public room with same players number") {
        daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber)(Participant("User1", userAddress))
          .flatMap(_ => dao.enterPublicRoom(playersNumber)(Participant("User2", userAddress)))
          .flatMap(_ => dao.deleteAndRecreatePublicRoom(playersNumber))
          .flatMap(_ => dao.publicRoomInfo(playersNumber))) map (_.participants shouldBe empty)
      }

      describe("should fail") {
        onWrongPlayersNumber(playersNumber => daoFuture.flatMap(_.deleteAndRecreatePublicRoom(playersNumber)))

        it("if room is not full") {
          recoverToSucceededIf[IllegalStateException] {
            daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber) flatMap (_ => dao.deleteAndRecreatePublicRoom(playersNumber)))
          }
        }
      }
    }
  }


  override protected def onWrongRoomID(test: String => Future[_]): Unit = {
    it("if roomId is empty") {
      recoverToSucceededIf[IllegalArgumentException](test(""))
    }
    it("if provided roomId is not present") {
      recoverToSucceededIf[NoSuchElementException](test("11111111"))
    }
  }

  override protected def onWrongPlayersNumber(test: Int => Future[_]): Unit = {
    it("if provided players number is less than 2") {
      recoverToSucceededIf[IllegalArgumentException](test(0))
    }
    it("if there's no room with such players number") {
      recoverToSucceededIf[NoSuchElementException](test(20))
    }
  }

  describe("The Helper shouldn't work") {
    describe("if not initialized") {
      val fakeRoomName = "Stanza"
      val fakeRoomID = "12342134"
      val fakePlayersNumber = 2
      
      val uninitializedDaoFuture: Future[RoomsLocalDAO] = vertx.fileSystem.readFileFuture("database/jdbc_config.json")
        .map(config => JDBCClient.createShared(vertx, new JsonObject(config)))
        .map(client => RoomsLocalDAO(client))

      it("createRoom") {
        uninitializedDaoFuture.flatMap(_.createRoom(fakeRoomName, fakePlayersNumber)).transform({
            case Failure(_: IllegalStateException) => Success(Unit)
            case _ => Failure(new Exception)
          }).map(_ => succeed)
      }
      it("enterRoom") {
        uninitializedDaoFuture.flatMap(_.enterRoom(fakeRoomID)).transform({
            case Failure(_: IllegalStateException) => Success(Unit)
            case _ => Failure(new Exception)
          }).map(_ => succeed)
      }
      it("roomInfo") {
        uninitializedDaoFuture.flatMap(_.roomInfo(fakeRoomID)).transform({
            case Failure(_: IllegalStateException) => Success(Unit)
            case _ => Failure(new Exception)
          }).map(_ => succeed)
      }
      it("exitRoom") {
        uninitializedDaoFuture.flatMap(_.exitRoom(fakeRoomID)).transform({
            case Failure(_: IllegalStateException) => Success(Unit)
            case _ => Failure(new Exception)
          }).map(_ => succeed)
      }
      it("listPublicRooms") {
        uninitializedDaoFuture.flatMap(_.listPublicRooms()).transform({
            case Failure(_: IllegalStateException) => Success(Unit)
            case _ => Failure(new Exception)
          }).map(_ => succeed)
      }
      it("enterPublicRoom") {
        uninitializedDaoFuture.flatMap(_.enterPublicRoom(fakePlayersNumber)).transform({
            case Failure(_: IllegalStateException) => Success(Unit)
            case _ => Failure(new Exception)
          }).map(_ => succeed)
      }
      it("publicRoomInfo") {
        uninitializedDaoFuture.flatMap(_.publicRoomInfo(fakePlayersNumber)).transform({
            case Failure(_: IllegalStateException) => Success(Unit)
            case _ => Failure(new Exception)
          }).map(_ => succeed)
      }
      it("exitPublicRoom") {
        uninitializedDaoFuture.flatMap(_.exitPublicRoom(fakePlayersNumber)).transform({
            case Failure(_: IllegalStateException) => Success(Unit)
            case _ => Failure(new Exception)
          }).map(_ => succeed)
      }
      it("deleteRoom") {
        uninitializedDaoFuture.flatMap(_.deleteRoom(fakeRoomID)).transform({
            case Failure(_: IllegalStateException) => Success(Unit)
            case _ => Failure(new Exception)
          }).map(_ => succeed)
      }
      it("deleteAndRecreatePublicRoom") {
        uninitializedDaoFuture.flatMap(_.deleteAndRecreatePublicRoom(fakePlayersNumber)).transform({
            case Failure(_: IllegalStateException) => Success(Unit)
            case _ => Failure(new Exception)
          }).map(_ => succeed)
      }
    }
  }
}
