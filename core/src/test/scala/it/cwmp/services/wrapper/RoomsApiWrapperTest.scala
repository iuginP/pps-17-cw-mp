package it.cwmp.services.wrapper

import it.cwmp.exceptions.HTTPException
import it.cwmp.testing.FutureMatchers
import it.cwmp.testing.rooms.RoomsWebServiceTesting
import org.scalatest.Assertion

import scala.concurrent.Future
import scala.util.Success

/**
  * Testing class for RoomsApiWrapper
  *
  * @author Enrico Siboni
  */
class RoomsApiWrapperTest extends RoomsWebServiceTesting with FutureMatchers {

  private val apiWrapper = RoomsApiWrapper()

  import apiWrapper._

  override protected def privateRoomCreationTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed returning roomID if parameters are correct") {
      createRoom(roomName, playersNumber) flatMap (_ should not be empty)
    }

    describe("should fail") {
      it("if roomName empty") {
        createRoom("", playersNumber).shouldFailWith[HTTPException]
      }
      it("if playersNumber not correct") {
        createRoom(roomName, 0).shouldFailWith[HTTPException]
      }
    }
  }

  override protected def privateRoomEnteringTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed when the input is valid and room non full") {
      createRoom(roomName, playersNumber) flatMap (roomID => enterRoom(roomID, participantList.head, notificationAddress)
        .flatMap(_ => cleanUpRoom(roomID, succeed)))
    }

    describe("should fail") {
      onWrongRoomID(enterRoom(_, participantList.head, notificationAddress))

      it("if user is already inside a room") {
        var createdRoom = ""
        createRoom(roomName, playersNumber).andThen { case Success(id) => createdRoom = id }
          .flatMap(roomID => enterRoom(roomID, participantList.head, notificationAddress)
            .flatMap(_ => enterRoom(roomID, participantList.head, notificationAddress)))
          .shouldFailWith[HTTPException]
          .flatMap(cleanUpRoom(createdRoom, _))
      }
      it("if the room is full") {
        val playersNumber = 2
        var createdRoom = ""
        createRoom(roomName, playersNumber).andThen { case Success(id) => createdRoom = id }
          .flatMap(roomID => enterRoom(roomID, participantList.head, notificationAddress)
            .flatMap(_ => enterRoom(roomID, participantList(1), notificationAddress)(tokenList(1)))
            .flatMap(_ => enterRoom(roomID, participantList(2), notificationAddress)(tokenList(2))))
          .shouldFailWith[HTTPException]
      }
    }
  }

  override protected def privateRoomInfoRetrievalTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomId is correct") {
      createRoom(roomName, playersNumber) flatMap roomInfo flatMap (_ => succeed)
    }
    it("should show user inside room") {
      createRoom(roomName, playersNumber) flatMap (roomID => enterRoom(roomID, participantList.head, notificationAddress)
        .flatMap(_ => roomInfo(roomID))
        .flatMap(_.participants should contain(participantList.head))
        .flatMap(cleanUpRoom(roomID, _)))
    }

    describe("should fail") {
      onWrongRoomID(roomInfo)
    }
  }

  override protected def privateRoomExitingTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomID is correct and user inside") {
      createRoom(roomName, playersNumber)
        .flatMap(roomID => enterRoom(roomID, participantList.head, notificationAddress)
          .flatMap(_ => exitRoom(roomID))) flatMap (_ => succeed)
    }
    it("user should not be inside after it") {
      createRoom(roomName, playersNumber)
        .flatMap(roomID => enterRoom(roomID, participantList.head, notificationAddress)
          .flatMap(_ => exitRoom(roomID))
          .flatMap(_ => roomInfo(roomID))) flatMap (_.participants shouldNot contain(participantList.head))
    }

    describe("should fail") {
      onWrongRoomID(exitRoom)

      it("if user is not inside the room") {
        createRoom(roomName, playersNumber).flatMap(exitRoom).shouldFailWith[HTTPException]
      }
      it("if user is inside another room") {
        var createdRoom = ""
        createRoom(roomName, playersNumber)
          .flatMap(roomID => createRoom(roomName, playersNumber).andThen { case Success(id) => createdRoom = id }
            .flatMap(otherRoomID => enterRoom(otherRoomID, participantList.head, notificationAddress)) flatMap (_ => exitRoom(roomID)))
          .shouldFailWith[HTTPException]
          .flatMap(cleanUpRoom(createdRoom, _))
      }
    }
  }

  override protected def publicRoomEnteringTests(playersNumber: Int): Unit = {
    describe("should succeed") {
      it("if room with such players number exists") {
        enterPublicRoom(playersNumber, participantList.head, notificationAddress) flatMap (_ => cleanUpRoom(playersNumber, succeed))
      }
      it("and the user should be inside it") {
        enterPublicRoom(playersNumber, participantList.head, notificationAddress)
          .flatMap(_ => publicRoomInfo(playersNumber))
          .flatMap(_.participants should contain(participantList.head))
          .flatMap(cleanUpRoom(playersNumber, _))
      }
      it("even when the room was filled in past") {
        enterPublicRoom(2, participantList.head, notificationAddress)
          .flatMap(_ => enterPublicRoom(2, participantList(1), notificationAddress)(tokenList(1)))
          .flatMap(_ => enterPublicRoom(2, participantList(2), notificationAddress)(tokenList(2)))
          .flatMap(_ => publicRoomInfo(2))
          .flatMap(_.participants should contain only participantList(2))
          .flatMap(cleanUpRoom(2, _)(tokenList(2)))
      }
    }

    describe("should fail") {
      onWrongPlayersNumber(enterPublicRoom(_, participantList.head, notificationAddress))

      it("if same user is already inside a room") {
        enterPublicRoom(2, participantList.head, notificationAddress)
          .flatMap(_ => enterPublicRoom(3, participantList.head, notificationAddress))
          .shouldFailWith[HTTPException]
          .flatMap(cleanUpRoom(2, _))
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
      (enterPublicRoom(playersNumber, participantList.head, notificationAddress) flatMap (_ => publicRoomInfo(playersNumber)))
        .flatMap(_.participants should contain(participantList.head))
        .flatMap(cleanUpRoom(playersNumber, _))
    }

    describe("should fail") {
      onWrongPlayersNumber(publicRoomInfo)
    }
  }

  override protected def publicRoomExitingTests(playersNumber: Int): Unit = {
    it("should succeed if players number is correct and user is inside") {
      (enterPublicRoom(playersNumber, participantList.head, notificationAddress) flatMap (_ => exitPublicRoom(playersNumber))) flatMap (_ => succeed)
    }
    it("user should not be inside after it") {
      enterPublicRoom(playersNumber, participantList.head, notificationAddress)
        .flatMap(_ => exitPublicRoom(playersNumber))
        .flatMap(_ => publicRoomInfo(playersNumber))
        .flatMap(_.participants shouldNot contain(participantList.head))
    }

    describe("should fail") {
      onWrongPlayersNumber(exitPublicRoom)

      it("if user is not inside the room") {
        exitPublicRoom(playersNumber).shouldFailWith[HTTPException]
      }
      it("if user is inside another room") {
        enterPublicRoom(2, participantList.head, notificationAddress)
          .flatMap(_ => exitPublicRoom(3))
          .shouldFailWith[HTTPException]
          .flatMap(cleanUpRoom(2, _))
      }
    }
  }

  override protected def onWrongRoomID(apiCall: String => Future[_]): Unit = {
    it("if roomID is empty") {
      apiCall("").shouldFailWith[HTTPException]
    }
    it("if provided roomID is not present") {
      apiCall("1233124").shouldFailWith[HTTPException]
    }
  }

  override protected def onWrongPlayersNumber(apiCall: Int => Future[_]): Unit = {
    it("if players number provided less than 2") {
      apiCall(0).shouldFailWith[HTTPException]
    }
    it("if room with such players number doesn't exist") {
      apiCall(20).shouldFailWith[HTTPException]
    }
  }

  describe("Room Api Wrapper") {
    val fakeRoomName = "Room"
    val fakePlayersNumber = 3
    val fakeRoomID = "12342134"

    describe("should fail calling") {
      describe("createRoom") {
        shouldThrowExceptionOnBadToken { token: String =>
          createRoom(fakeRoomName, fakePlayersNumber)(token).shouldFailWith[HTTPException]
        }
      }
      describe("enterRoom") {
        shouldThrowExceptionOnBadToken { token: String =>
          enterRoom(fakeRoomID, participantList.head, notificationAddress)(token).shouldFailWith[HTTPException]
        }
      }
      describe("roomInfo") {
        shouldThrowExceptionOnBadToken { token: String =>
          roomInfo(fakeRoomID)(token).shouldFailWith[HTTPException]
        }
      }
      describe("exitRoom") {
        shouldThrowExceptionOnBadToken { token: String =>
          exitRoom(fakeRoomID)(token).shouldFailWith[HTTPException]
        }
      }
      describe("listPublicRooms") {
        shouldThrowExceptionOnBadToken { token: String =>
          listPublicRooms()(token).shouldFailWith[HTTPException]
        }
      }
      describe("enterPublicRoom") {
        shouldThrowExceptionOnBadToken { token: String =>
          enterPublicRoom(fakePlayersNumber, participantList.head, notificationAddress)(token).shouldFailWith[HTTPException]
        }
      }
      describe("publicRoomInfo") {
        shouldThrowExceptionOnBadToken { token: String =>
          publicRoomInfo(fakePlayersNumber)(token).shouldFailWith[HTTPException]
        }
      }
      describe("exitPublicRoom") {
        shouldThrowExceptionOnBadToken { token: String =>
          exitPublicRoom(fakePlayersNumber)(token).shouldFailWith[HTTPException]
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

  /**
    * Cleans up the provided room, exiting the user with passed token
    */
  private def cleanUpRoom(roomID: String, assertion: Assertion)(implicit userToken: String) =
    exitRoom(roomID)(userToken) map (_ => assertion)

  /**
    * Cleans up the provided public room, exiting player with passed token
    */
  private def cleanUpRoom(playersNumber: Int, assertion: Assertion)(implicit userToken: String) =
    exitPublicRoom(playersNumber)(userToken) map (_ => assertion)
}
