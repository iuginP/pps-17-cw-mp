package it.cwmp.room

import it.cwmp.model.User
import it.cwmp.testing.VertxTest
import org.scalatest.{BeforeAndAfterEach, Matchers}

import scala.concurrent.Future

/**
  * Test class for local RoomsDAO
  *
  * @author Enrico Siboni
  */
class RoomsLocalDAOTest extends VertxTest with Matchers with BeforeAndAfterEach {

  private var daoFuture: Future[RoomsDAO] = _

  private val roomName = "Stanza"
  private val playersNumber = 4

  private val user: User = User("Enrico")

  override protected def beforeEach(): Unit = {
    val localDAO = RoomsDAO(vertx)
    daoFuture = localDAO.initialize().map(_ => localDAO)
  }

  describe("Private Room") {

    describe("Creation") {
      it("should succeed returning roomId, if parameters are correct") {
        daoFuture.flatMap(_.createRoom(roomName, playersNumber))
          .flatMap(roomID => roomID.nonEmpty shouldBe true)
      }
      describe("should fail") {
        it("if roomName empty") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(_.createRoom("", playersNumber))
          }
        }
        it("if playersNumber less than 2") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(_.createRoom(roomName, 0))
          }
        }
      }
    }

    describe("Entering") {
      it("should succeed if roomId is provided") {
        daoFuture
          .flatMap(dao => dao.createRoom(roomName, playersNumber)
            .flatMap(roomID => dao.enterRoom(roomID)(user))
            .map(_ => succeed))
      }

      it("user should be inside after it") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.enterRoom(roomID)(user)
            .flatMap(_ => dao.roomInfo(roomID))
            .flatMap(_.get.participants should contain(user))))
      }

      describe("should fail") {
        it("if roomId is empty") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(_.enterRoom("")(user))
          }
        }

        it("if provided roomId is not present") {
          recoverToSucceededIf[NoSuchElementException] {
            daoFuture.flatMap(_.enterRoom("11111111")(user))
          }
        }

        it("if user is already inside a room") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
              .flatMap(roomID => dao.enterRoom(roomID)(user)
                .flatMap(_ => dao.enterRoom(roomID)(user))))
          }
        }
      }
    }

    describe("Retrieving Info") {
      it("should succeed if roomId is correct") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.roomInfo(roomID)))
          .flatMap(_.get.participants shouldBe empty)
      }

      it("should return empty option if room not present") {
        daoFuture.flatMap(_.roomInfo("1111111"))
          .flatMap(_ shouldBe empty)
      }

      describe("should fail") {
        it("if room id is empty") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(_.roomInfo(""))
          }
        }

      }
    }

    describe("Exit Room") {
      it("should succeed if roomID is correct and user inside") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.enterRoom(roomID)(user)
            .flatMap(_ => dao.exitRoom(roomID)(user))))
          .map(_ => succeed)
      }

      describe("should fail") {
        it("if roomId is empty") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(_.exitRoom("")(user))
          }
        }

        it("if provided roomId is not present") {
          recoverToSucceededIf[NoSuchElementException] {
            daoFuture.flatMap(_.exitRoom("11221211")(user))
          }
        }

        it("if user is not inside the room") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
              .flatMap(roomID => dao.exitRoom(roomID)(user)))
          }
        }

        it("if user is inside another room") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
              .flatMap(roomID => dao.createRoom(roomName, playersNumber)
                .flatMap(otherRoomID => dao.enterRoom(otherRoomID)(user))
                .flatMap(_ => dao.exitRoom(roomID)(user))))
          }
        }
      }
    }
  }

  describe("Public Room") {
    it("listing should be nonEmpty") {
      daoFuture.flatMap(_.listPublicRooms()).flatMap(_ should not be empty)
    }

    it("should show only public rooms") {
      daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
        .flatMap(_ => dao.listPublicRooms()))
        .flatMap(_.forall(_.identifier.contains(RoomsDAO.publicPrefix)) shouldBe true)
    }
  }

  describe("The Helper shouldn't work") {
    describe("if not initialized") {
      val fakeRoomID = "12342134"
      implicit val fakeUser: User = User("Enrico")
      it("createRoom") {
        recoverToSucceededIf[IllegalStateException](RoomsDAO(vertx).createRoom(roomName, playersNumber))
      }
      it("enterRoom") {
        recoverToSucceededIf[IllegalStateException](RoomsDAO(vertx).enterRoom(fakeRoomID))
      }
      it("roomInfo") {
        recoverToSucceededIf[IllegalStateException](RoomsDAO(vertx).roomInfo(fakeRoomID))
      }
      it("exitRoom") {
        recoverToSucceededIf[IllegalStateException](RoomsDAO(vertx).exitRoom(fakeRoomID))
      }
      it("listPublicRooms") {
        recoverToSucceededIf[IllegalStateException](RoomsDAO(vertx).listPublicRooms())
      }
      it("enterPublicRoom") {
        recoverToSucceededIf[IllegalStateException](RoomsDAO(vertx).enterPublicRoom(playersNumber))
      }
      it("publicRoomInfo") {
        recoverToSucceededIf[IllegalStateException](RoomsDAO(vertx).publicRoomInfo(playersNumber))
      }
      it("exitPublicRoom") {
        recoverToSucceededIf[IllegalStateException](RoomsDAO(vertx).exitPublicRoom(playersNumber))
      }
    }
  }
}
