package it.cwmp.testing.rooms

import it.cwmp.testing.VertxTest
import org.scalatest.Matchers

import scala.concurrent.Future

/**
  * A base class that provides common tests structure for Rooms
  *
  * @author Enrico Siboni
  */
abstract class RoomsTesting extends VertxTest with Matchers {

  private val roomName = "Stanza"
  private val privateRoomPlayersNumber = 2
  private val publicRoomPlayersNumber = 2

  protected val TOO_BIG_PLAYERS_NUMBER = 20

  describe("Private Room") {
    describe("Creation") {
      privateRoomCreationTests(roomName, privateRoomPlayersNumber)
    }

    describe("Entering") {
      privateRoomEnteringTests(roomName, privateRoomPlayersNumber)
    }

    describe("Retrieving Info") {
      privateRoomInfoRetrievalTests(roomName, privateRoomPlayersNumber)
    }

    describe("Exit Room") {
      privateRoomExitingTests(roomName, privateRoomPlayersNumber)
    }
  }

  describe("Public Room") {
    describe("Listing") {
      publicRoomListingTests(publicRoomPlayersNumber)
    }

    describe("Entering") {
      publicRoomEnteringTests(publicRoomPlayersNumber)
    }

    describe("Retrieving Info") {
      publicRoomInfoRetrievalTests(publicRoomPlayersNumber)
    }

    describe("Exiting") {
      publicRoomExitingTests(publicRoomPlayersNumber)
    }
  }

  protected def privateRoomCreationTests(roomName: String, playersNumber: Int)

  protected def privateRoomEnteringTests(roomName: String, playersNumber: Int)

  protected def privateRoomInfoRetrievalTests(roomName: String, playersNumber: Int)

  protected def privateRoomExitingTests(roomName: String, playersNumber: Int)

  protected def publicRoomEnteringTests(playersNumber: Int)

  protected def publicRoomListingTests(playersNumber: Int)

  protected def publicRoomInfoRetrievalTests(playersNumber: Int)

  protected def publicRoomExitingTests(playersNumber: Int)

  /**
    * Describes what should happen when testing the given function with wrong roomID
    */
  protected def onWrongRoomID(test: String => Future[_])

  /**
    * Describes what should happen when testing the given function with wrong players number
    */
  protected def onWrongPlayersNumber(test: Int => Future[_])
}
