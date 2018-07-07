package it.cwmp.controller.rooms

import it.cwmp.testing.server.rooms.RoomsServiceWebTesting
import org.scalatest.Matchers

/**
  * Testing class for RoomsApiWrapper
  *
  * @author Enrico Siboni
  */
class RoomsApiWrapperTest extends RoomsServiceWebTesting with Matchers {

  private val apiWrapper = RoomsApiWrapper()

  private val roomName = "Stanza"
  private val playersNumber = 2

  describe("Private Room") {
    describe("Creation") {
      it("should succeed returning roomID if parameters are correct") {
        apiWrapper.createRoom(roomName, playersNumber)(myFirstCorrectToken)
          .flatMap(_ should not be empty)
      }

      describe("should fail") {
        it("if roomName empty") {
          recoverToSucceededIf[RoomsServiceException] {
            apiWrapper.createRoom("", playersNumber)(myFirstCorrectToken)
          }
        }
        it("if playersNumber not correct") {
          recoverToSucceededIf[RoomsServiceException] {
            apiWrapper.createRoom(roomName, 0)(myFirstCorrectToken)
          }
        }
      }
    }
  }
  // TODO: to implement

}
