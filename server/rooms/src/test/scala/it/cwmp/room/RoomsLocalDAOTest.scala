package it.cwmp.room

import it.cwmp.testing.VerticleTesting
import org.scalatest.{BeforeAndAfterAll, Matchers}

import scala.concurrent.Future

/**
  * Test class for local RoomsDAO
  *
  * @author Enrico Siboni
  */
class RoomsLocalDAOTest extends VerticleTesting[RoomsServiceVerticle] with Matchers with BeforeAndAfterAll {
  // TODO: create test base class that not deploys a verticle that's not used

  private var daoFuture: Future[RoomsDAO] = _

  val roomName = "Stanza"
  val playersNumber = 4

  override protected def beforeAll(): Unit = {
    val localDAO = RoomsDAO(vertx)
    daoFuture = localDAO.initialize().map(_ => localDAO)
  }

  describe("The Helper shouldn't work") {
    it("if not initialized") {
      recoverToSucceededIf[IllegalStateException](RoomsDAO(vertx).listPublicRooms())
    }
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
  }

  describe("Public Room") {
    it("listing should be nonEmpty") {
      daoFuture.flatMap(_.listPublicRooms())
        .flatMap(_.nonEmpty shouldBe true)
    }

    it("should show only public rooms") {
      daoFuture.flatMap(dao =>
        dao.createRoom(roomName, playersNumber).flatMap(_ =>
          dao.listPublicRooms()).map(_.find(
          // there shouldn't be identifiers with "public" prefix
          !_.identifier.contains(RoomsDAO.publicPrefix)) shouldBe None)
      )
    }
  }
}
