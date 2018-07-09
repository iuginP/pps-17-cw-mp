package it.cwmp.controller.rooms

import it.cwmp.testing.server.rooms.RoomsWebServiceTesting
import org.scalatest.Assertion

import scala.concurrent.Future

/**
  * Testing class for RoomsApiWrapper
  *
  * @author Enrico Siboni
  */
class RoomsApiWrapperTest extends RoomsWebServiceTesting {

  private val apiWrapper = RoomsApiWrapper()

  import apiWrapper._

  override protected def privateRoomCreationTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed returning roomID if parameters are correct") {
      createRoom(roomName, playersNumber) flatMap (_ should not be empty)
    }

    describe("should fail") {
      it("if roomName empty") {
        recoverToSucceededIf[RoomsServiceException](createRoom("", playersNumber))
      }
      it("if playersNumber not correct") {
        recoverToSucceededIf[RoomsServiceException](createRoom(roomName, 0))
      }
    }
  }

  override protected def privateRoomEnteringTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed when the input is valid and room non full") {
      createRoom(roomName, playersNumber) flatMap enterRoom flatMap (_ => succeed)
    }

    describe("should fail") {
      onWrongRoomID(enterRoom)

      it("if user is already inside a room") {
        recoverToSucceededIf[RoomsServiceException] {
          createRoom(roomName, playersNumber) flatMap (roomID => enterRoom(roomID) flatMap (_ => enterRoom(roomID)))
        }
      }
      it("if the room is full") {
        val playersNumber = 2
        recoverToSucceededIf[RoomsServiceException] {
          createRoom(roomName, playersNumber)
            .flatMap(roomID => enterRoom(roomID)
              .flatMap(_ => enterRoom(roomID)(mySecondAuthorizedUser, mySecondCorrectToken))
              .flatMap(_ => enterRoom(roomID)(myThirdAuthorizedUser, myThirdCorrectToken)))
        }
      }
    }
  }

  override protected def privateRoomInfoRetrievalTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomId is correct") {
      createRoom(roomName, playersNumber) flatMap roomInfo flatMap (_ => succeed)
    }
    it("should show user inside room") {
      createRoom(roomName, playersNumber)
        .flatMap(roomID => enterRoom(roomID)
          .flatMap(_ => roomInfo(roomID))) flatMap (_.participants should contain(myFirstAuthorizedUser))
    }

    describe("should fail") {
      onWrongRoomID(roomInfo)
    }
  }

  override protected def privateRoomExitingTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomID is correct and user inside") {
      createRoom(roomName, playersNumber)
        .flatMap(roomID => enterRoom(roomID)
          .flatMap(_ => exitRoom(roomID))) flatMap (_ => succeed)
    }
    it("user should not be inside after it") {
      createRoom(roomName, playersNumber)
        .flatMap(roomID => enterRoom(roomID)
          .flatMap(_ => exitRoom(roomID))
          .flatMap(_ => roomInfo(roomID))) flatMap (_.participants shouldNot contain(myFirstAuthorizedUser))
    }

    describe("should fail") {
      onWrongRoomID(exitRoom)

      it("if user is not inside the room") {
        recoverToSucceededIf[RoomsServiceException](createRoom(roomName, playersNumber) flatMap exitRoom)
      }
      it("if user is inside another room") {
        recoverToSucceededIf[RoomsServiceException] {
          createRoom(roomName, playersNumber)
            .flatMap(roomID => createRoom(roomName, playersNumber)
              .flatMap(otherRoomID => enterRoom(otherRoomID)) flatMap (_ => exitRoom(roomID)))
        }
      }
    }
  }

  override protected def publicRoomEnteringTests(playersNumber: Int): Unit = {
    describe("should succeed") {
      it("if room with such players number exists") {
        enterPublicRoom(playersNumber) flatMap (_ => succeed)
      }
      it("and the user should be inside it") {
        enterPublicRoom(playersNumber)
          .flatMap(_ => publicRoomInfo(playersNumber))
          .flatMap(_.participants should contain(myFirstAuthorizedUser))
      }
      it("even when the room was filled in past") {
        enterPublicRoom(2)
          .flatMap(_ => enterPublicRoom(2)(mySecondAuthorizedUser, mySecondCorrectToken))
          .flatMap(_ => enterPublicRoom(2)(myThirdAuthorizedUser, myThirdCorrectToken))
          .flatMap(_ => publicRoomInfo(2))
          .flatMap(_.participants should contain only myThirdAuthorizedUser)
      }
    }

    describe("should fail") {
      onWrongPlayersNumber(enterPublicRoom)

      it("if same user is already inside a room") {
        recoverToSucceededIf[RoomsServiceException] {
          enterPublicRoom(2) flatMap (_ => enterPublicRoom(3))
        }
      }
    }
  }

  override protected def publicRoomListingTests(playersNumber: Int): Unit = {
    it("should be non empty") {
      listPublicRooms() flatMap (_ should not be empty)
    }
  }

  override protected def publicRoomInfoRetrievalTests(playersNumber: Int): Unit = {
    it("should succeed if provided playersNumber is correct") {
      publicRoomInfo(playersNumber) flatMap (_ => succeed)
    }
    it("should show entered players") {
      (enterPublicRoom(playersNumber) flatMap (_ => publicRoomInfo(playersNumber)))
        .flatMap(_.participants should contain(myFirstAuthorizedUser))
    }

    describe("should fail") {
      onWrongPlayersNumber(publicRoomInfo)
    }
  }

  override protected def publicRoomExitingTests(playersNumber: Int): Unit = {
    it("should succeed if players number is correct and user is inside") {
      (enterPublicRoom(playersNumber) flatMap (_ => exitPublicRoom(playersNumber))) flatMap (_ => succeed)
    }
    it("user should not be inside after it") {
      enterPublicRoom(playersNumber)
        .flatMap(_ => exitPublicRoom(playersNumber))
        .flatMap(_ => publicRoomInfo(playersNumber))
        .flatMap(_.participants shouldNot contain(myFirstAuthorizedUser))
    }

    describe("should fail") {
      onWrongPlayersNumber(exitPublicRoom)

      it("if user is not inside the room") {
        recoverToSucceededIf[RoomsServiceException](exitPublicRoom(playersNumber))
      }
      it("if user is inside another room") {
        recoverToSucceededIf[RoomsServiceException] {
          enterPublicRoom(2) flatMap (_ => exitPublicRoom(3))
        }
      }
    }
  }

  override protected def onWrongRoomID(apiCall: String => Future[_]): Unit = {
    it("if roomID is empty") {
      recoverToSucceededIf[RoomsServiceException](apiCall(""))
    }
    it("if provided roomID is not present") {
      recoverToSucceededIf[RoomsServiceException](apiCall("1233124"))
    }
  }

  override protected def onWrongPlayersNumber(apiCall: (Int) => Future[_]): Unit = {
    it("if players number provided less than 2") {
      recoverToSucceededIf[RoomsServiceException](apiCall(0))
    }
    it("if room with such players number doesn't exist") {
      recoverToSucceededIf[RoomsServiceException](apiCall(20))
    }
  }

  describe("Room Api Wrapper") {
    val fakeRoomName = "Room"
    val fakePlayersNumber = 3
    val fakeRoomID = "12342134"

    describe("should fail calling") {
      describe("createRoom") {
        shouldThrowExceptionOnBadToken { (token: String) =>
          recoverToSucceededIf[RoomsServiceException](createRoom(fakeRoomName, fakePlayersNumber)(token))
        }
      }
      describe("enterRoom") {
        shouldThrowExceptionOnBadToken { (token: String) =>
          recoverToSucceededIf[RoomsServiceException](enterRoom(fakeRoomID)(myFirstAuthorizedUser, token))
        }
      }
      describe("roomInfo") {
        shouldThrowExceptionOnBadToken { (token: String) =>
          recoverToSucceededIf[RoomsServiceException](roomInfo(fakeRoomID)(token))
        }
      }
      describe("exitRoom") {
        shouldThrowExceptionOnBadToken { (token: String) =>
          recoverToSucceededIf[RoomsServiceException](exitRoom(fakeRoomID)(token))
        }
      }
      describe("listPublicRooms") {
        shouldThrowExceptionOnBadToken { (token: String) =>
          recoverToSucceededIf[RoomsServiceException](listPublicRooms()(token))
        }
      }
      describe("enterPublicRoom") {
        shouldThrowExceptionOnBadToken { (token: String) =>
          recoverToSucceededIf[RoomsServiceException](enterPublicRoom(fakePlayersNumber)(myFirstAuthorizedUser, token))
        }
      }
      describe("publicRoomInfo") {
        shouldThrowExceptionOnBadToken { (token: String) =>
          recoverToSucceededIf[RoomsServiceException](publicRoomInfo(fakePlayersNumber)(token))
        }
      }
      describe("exitPublicRoom") {
        shouldThrowExceptionOnBadToken { (token: String) =>
          recoverToSucceededIf[RoomsServiceException](exitPublicRoom(fakePlayersNumber)(token))
        }
      }

      /**
        * Describes the behaviour of apiWrapper if used with bad tokens
        */
      def shouldThrowExceptionOnBadToken(apiUseWithToken: String => Future[Assertion]) {
        it(s"if token wrong") {
          apiUseWithToken("token")
        }
        it(s"if token isn't valid") {
          apiUseWithToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68")
        }
      }
    }
  }
}
