package it.cwmp.room

import it.cwmp.controller.rooms.RoomsApiWrapper
import it.cwmp.model.{Address, User}
import it.cwmp.testing.VertxTest
import org.scalatest.{BeforeAndAfterEach, Matchers}

import scala.concurrent.Future

/**
  * Test class for local RoomsDAO
  *
  * @author Enrico Siboni
  */
class RoomsLocalDAOTest extends VertxTest with Matchers with BeforeAndAfterEach {

  private var daoFuture: Future[RoomLocalDAO] = _

  private val roomName = "Stanza"

  private val userAddress = "fakeAddress"
  private val user: User with Address = User("Enrico", userAddress)

  override protected def beforeEach(): Unit = {
    val localDAO = RoomLocalDAO(vertx)
    daoFuture = localDAO.initialize().map(_ => localDAO)
  }

  describe("Private Room") {
    val playersNumber = 2

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
            .flatMap(_.participants should contain(user))))
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
          recoverToSucceededIf[IllegalStateException] {
            daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
              .flatMap(roomID => dao.enterRoom(roomID)(user)
                .flatMap(_ => dao.enterRoom(roomID)(user))))
          }
        }
        it("if the room is full") {
          recoverToSucceededIf[IllegalStateException] {
            val playersNumber = 2
            daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
              .flatMap(roomID => dao.enterRoom(roomID)(User("User1", userAddress))
                .flatMap(_ => dao.enterRoom(roomID)(User("User2", userAddress)))
                .flatMap(_ => dao.enterRoom(roomID)(User("User3", userAddress)))
              ))
          }
        }
      }
    }

    describe("Retrieving Info") {
      it("should succeed if roomId is correct") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.roomInfo(roomID)))
          .flatMap(_.participants shouldBe empty)
      }

      describe("should fail") {
        it("if room id is empty") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(_.roomInfo(""))
          }
        }
        it("if room is not present") {
          recoverToSucceededIf[NoSuchElementException] {
            daoFuture.flatMap(_.roomInfo("1111111"))
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
      it("user should not be inside after it") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.enterRoom(roomID)(user)
            .flatMap(_ => dao.exitRoom(roomID)(user))
            .flatMap(_ => dao.roomInfo(roomID))
            .flatMap(_.participants shouldNot contain(user))))
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
          recoverToSucceededIf[IllegalStateException] {
            daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
              .flatMap(roomID => dao.exitRoom(roomID)(user)))
          }
        }
        it("if user is inside another room") {
          recoverToSucceededIf[IllegalStateException] {
            daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
              .flatMap(roomID => dao.createRoom(roomName, playersNumber)
                .flatMap(otherRoomID => dao.enterRoom(otherRoomID)(user))
                .flatMap(_ => dao.exitRoom(roomID)(user))))
          }
        }
      }
    }

    describe("Deletion") {
      it("should succeed if room is full") {
        val playersNumber = 2
        daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
          .flatMap(roomID => dao.enterRoom(roomID)(User("User1", userAddress))
            .flatMap(_ => dao.enterRoom(roomID)(User("User2", userAddress)))
            .flatMap(_ => dao.deleteRoom(roomID))))
          .map(_ => succeed)
      }
      it("should succeed remove the room") {
        val playersNumber = 2
        recoverToSucceededIf[NoSuchElementException] {
          daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
            .flatMap(roomID => dao.enterRoom(roomID)(User("User1", userAddress))
              .flatMap(_ => dao.enterRoom(roomID)(User("User2", userAddress)))
              .flatMap(_ => dao.deleteRoom(roomID))
              .flatMap(_ => dao.roomInfo(roomID))))
        }
      }

      describe("should fail") {
        it("if roomId is empty") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(_.deleteRoom(""))
          }
        }
        it("if provided roomId is not present") {
          recoverToSucceededIf[NoSuchElementException] {
            daoFuture.flatMap(_.deleteRoom("11221211"))
          }
        }
        it("if room is not full") {
          recoverToSucceededIf[IllegalStateException] {
            daoFuture.flatMap(dao => dao.createRoom(roomName, playersNumber)
              .flatMap(roomID => dao.enterRoom(roomID)(user)
                .flatMap(_ => dao.deleteRoom(roomID))))
          }
        }
      }
    }
  }

  describe("Public Room") {
    val publicRoomPlayersNumber = 2

    describe("Listing") {
      it("should be nonEmpty") {
        daoFuture.flatMap(_.listPublicRooms()).flatMap(_ should not be empty)
      }
      it("should show only public rooms") {
        daoFuture.flatMap(dao => dao.createRoom(roomName, publicRoomPlayersNumber)
          .flatMap(_ => dao.listPublicRooms()))
          .flatMap(_.forall(_.identifier.contains(RoomsApiWrapper.publicPrefix)) shouldBe true)
      }
    }

    describe("Entering") {
      describe("should succeed") {
        it("if room with such players number exists") {
          daoFuture.flatMap(_.enterPublicRoom(publicRoomPlayersNumber)(user))
            .map(_ => succeed)
        }
        it("and the user should be inside it") {
          daoFuture.flatMap(dao => dao.enterPublicRoom(publicRoomPlayersNumber)(user)
            .flatMap(_ => dao.publicRoomInfo(publicRoomPlayersNumber))
            .flatMap(_.participants should contain(user)))
        }
      }

      describe("should fail") {
        it("if provided players number is less than 2") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(_.enterPublicRoom(0)(user))
          }
        }
        it("if there's no room with such players number") {
          recoverToSucceededIf[NoSuchElementException] {
            daoFuture.flatMap(_.enterPublicRoom(20)(user))
          }
        }
        it("if user is already inside a room") {
          recoverToSucceededIf[IllegalStateException] {
            daoFuture.flatMap(dao => dao.enterPublicRoom(2)(user)
              .flatMap(_ => dao.enterPublicRoom(3)(user)))
          }
        }
        it("if room is full") {
          recoverToSucceededIf[IllegalStateException] {
            daoFuture.flatMap(dao => dao.enterPublicRoom(2)(User("User1", userAddress))
              .flatMap(_ => dao.enterPublicRoom(2)(User("User2", userAddress)))
              .flatMap(_ => dao.enterPublicRoom(2)(User("User3", userAddress))))
          }
        }
      }
    }

    describe("Retrieving Info") {
      it("should succeed if provided playersNumber is correct") {
        daoFuture.flatMap(_.publicRoomInfo(publicRoomPlayersNumber))
          .flatMap(_.participants shouldBe empty)
      }
      it("should show entered players") {
        daoFuture.flatMap(dao => dao.enterPublicRoom(publicRoomPlayersNumber)(user)
          .flatMap(_ => dao.publicRoomInfo(publicRoomPlayersNumber)))
          .flatMap(_.participants should contain(user))
      }

      describe("should fail") {
        it("if playersNumber provided is less than 2") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(_.publicRoomInfo(0))
          }
        }
        it("if room with such playersNumber doesn't exist") {
          recoverToSucceededIf[NoSuchElementException] {
            daoFuture.flatMap(_.publicRoomInfo(20))
          }
        }
      }
    }

    describe("Exiting") {
      it("should succeed if players number is correct and user is inside") {
        daoFuture.flatMap(dao => dao.enterPublicRoom(publicRoomPlayersNumber)(user)
          .flatMap(_ => dao.exitPublicRoom(publicRoomPlayersNumber)(user)))
          .flatMap(_ => succeed)
      }
      it("user should not be inside after it") {
        daoFuture.flatMap(dao => dao.enterPublicRoom(publicRoomPlayersNumber)(user)
          .flatMap(_ => dao.exitPublicRoom(publicRoomPlayersNumber)(user))
          .flatMap(_ => dao.publicRoomInfo(publicRoomPlayersNumber)))
          .flatMap(_.participants shouldNot contain(user))
      }

      describe("should fail") {
        it("if playersNumber provided is less than 2") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(_.exitPublicRoom(0)(user))
          }
        }
        it("if room with such playersNumber doesn't exist") {
          recoverToSucceededIf[NoSuchElementException] {
            daoFuture.flatMap(_.exitPublicRoom(20)(user))
          }
        }
        it("if user is not inside the room") {
          recoverToSucceededIf[IllegalStateException] {
            daoFuture.flatMap(_.exitPublicRoom(publicRoomPlayersNumber)(user))
          }
        }
        it("if user is inside another room") {
          recoverToSucceededIf[IllegalStateException] {
            daoFuture.flatMap(dao => dao.enterPublicRoom(publicRoomPlayersNumber)(user)
              .flatMap(_ => dao.exitPublicRoom(publicRoomPlayersNumber + 1)(user)))
          }
        }
      }
    }

    describe("Deletion") {
      it("should succeed if room is full, and recreate an empty public room with same players number") {
        val playersNumber = 2
        daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber)(User("User1", userAddress))
          .flatMap(_ => dao.enterPublicRoom(playersNumber)(User("User2", userAddress)))
          .flatMap(_ => dao.deleteAndRecreatePublicRoom(playersNumber))
          .flatMap(_ => dao.publicRoomInfo(playersNumber)))
          .map(publicRoom => publicRoom.participants shouldBe empty)
      }

      describe("should fail") {
        it("if playersNumber provided is less than 2") {
          recoverToSucceededIf[IllegalArgumentException] {
            daoFuture.flatMap(_.deleteAndRecreatePublicRoom(0))
          }
        }
        it("if room with such playersNumber doesn't exist") {
          recoverToSucceededIf[NoSuchElementException] {
            daoFuture.flatMap(_.deleteAndRecreatePublicRoom(20))
          }
        }
        it("if room is not full") {
          val playersNumber = 2
          recoverToSucceededIf[IllegalStateException] {
            daoFuture.flatMap(dao => dao.enterPublicRoom(playersNumber)(user)
              .flatMap(_ => dao.deleteAndRecreatePublicRoom(playersNumber)))
          }
        }
      }
    }
  }

  describe("The Helper shouldn't work") {
    describe("if not initialized") {
      val fakeRoomID = "12342134"
      val playersNumber = 2
      implicit val fakeUser: User with Address = User("Enrico", userAddress)
      it("createRoom") {
        recoverToSucceededIf[IllegalStateException](RoomLocalDAO(vertx).createRoom(roomName, playersNumber))
      }
      it("enterRoom") {
        recoverToSucceededIf[IllegalStateException](RoomLocalDAO(vertx).enterRoom(fakeRoomID))
      }
      it("roomInfo") {
        recoverToSucceededIf[IllegalStateException](RoomLocalDAO(vertx).roomInfo(fakeRoomID))
      }
      it("exitRoom") {
        recoverToSucceededIf[IllegalStateException](RoomLocalDAO(vertx).exitRoom(fakeRoomID))
      }
      it("listPublicRooms") {
        recoverToSucceededIf[IllegalStateException](RoomLocalDAO(vertx).listPublicRooms())
      }
      it("enterPublicRoom") {
        recoverToSucceededIf[IllegalStateException](RoomLocalDAO(vertx).enterPublicRoom(playersNumber))
      }
      it("publicRoomInfo") {
        recoverToSucceededIf[IllegalStateException](RoomLocalDAO(vertx).publicRoomInfo(playersNumber))
      }
      it("exitPublicRoom") {
        recoverToSucceededIf[IllegalStateException](RoomLocalDAO(vertx).exitPublicRoom(playersNumber))
      }
      it("deleteRoom") {
        recoverToSucceededIf[IllegalStateException](RoomLocalDAO(vertx).deleteRoom(fakeRoomID))
      }
      it("deleteAndRecreatePublicRoom") {
        recoverToSucceededIf[IllegalStateException](RoomLocalDAO(vertx).deleteAndRecreatePublicRoom(playersNumber))
      }
    }
  }
}
