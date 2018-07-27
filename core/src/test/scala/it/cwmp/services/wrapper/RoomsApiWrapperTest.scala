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

  //noinspection ScalaStyle
  import apiWrapper._

  override protected def privateRoomCreationTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed returning roomID if parameters are correct") {
      for (roomID <- createRoom(roomName, playersNumber)) yield roomID should not be empty
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
      for (roomID <- createRoom(roomName, playersNumber);
           _ <- enterRoom(roomID, participantList.head, notificationAddress);
           _ <- cleanUpRoom(roomID)) yield succeed
    }

    describe("should fail") {
      onWrongRoomID(enterRoom(_, participantList.head, notificationAddress))

      it("if user is already inside a room") {
        for (roomID <- createRoom(roomName, playersNumber);
             _ <- enterRoom(roomID, participantList.head, notificationAddress);
             assertion <- enterRoom(roomID, participantList.head, notificationAddress).shouldFailWith[HTTPException];
             _ <- cleanUpRoom(roomID)) yield assertion
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
      for (roomID <- createRoom(roomName, playersNumber);
           _ <- enterRoom(roomID, participantList.head, notificationAddress);
           roomInfo <- roomInfo(roomID);
           assertion <- roomInfo.participants should contain(participantList.head);
           _ <- cleanUpRoom(roomID)) yield assertion
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
        for (roomID <- createRoom(roomName, playersNumber);
             otherRoomID <- createRoom(roomName, playersNumber);
             _ <- enterRoom(otherRoomID, participantList.head, notificationAddress);
             assertion <- exitRoom(roomID).shouldFailWith[HTTPException];
             _ <- cleanUpRoom(otherRoomID)) yield assertion
      }
    }
  }

  override protected def publicRoomEnteringTests(playersNumber: Int): Unit = {
    describe("should succeed") {
      it("if room with such players number exists") {
        for (_ <- enterPublicRoom(playersNumber, participantList.head, notificationAddress);
             _ <- cleanUpRoom(playersNumber)) yield succeed
      }
      it("and the user should be inside it") {
        for (_ <- enterPublicRoom(playersNumber, participantList.head, notificationAddress);
             roomInfo <- publicRoomInfo(playersNumber);
             assertion <- roomInfo.participants should contain(participantList.head);
             _ <- cleanUpRoom(playersNumber)) yield assertion
      }
      it("even when the room was filled in past") {
        for (_ <- enterPublicRoom(2, participantList.head, notificationAddress);
             _ <- enterPublicRoom(2, participantList(1), notificationAddress)(tokenList(1));
             _ <- enterPublicRoom(2, participantList(2), notificationAddress)(tokenList(2));
             roomInfo <- publicRoomInfo(2);
             assertion <- roomInfo.participants should contain only participantList(2);
             _ <- cleanUpRoom(2)(tokenList(2))) yield assertion
      }
    }

    describe("should fail") {
      onWrongPlayersNumber(enterPublicRoom(_, participantList.head, notificationAddress))

      it("if same user is already inside a room") {
        for (_ <- enterPublicRoom(2, participantList.head, notificationAddress);
             assertion <- enterPublicRoom(3, participantList.head, notificationAddress).shouldFailWith[HTTPException];
             _ <- cleanUpRoom(2)) yield assertion
      }
    }
  }

  override protected def publicRoomListingTests(playersNumber: Int): Unit = {
    it("should be non empty") {
      listPublicRooms() flatMap (_ should not be empty)
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

  override protected def publicRoomInfoRetrievalTests(playersNumber: Int): Unit = {
    it("should succeed if provided playersNumber is correct") {
      publicRoomInfo(playersNumber) flatMap (_ => succeed)
    }
    it("should show entered players") {
      for (_ <- enterPublicRoom(playersNumber, participantList.head, notificationAddress);
           roomInfo <- publicRoomInfo(playersNumber);
           assertion <- roomInfo.participants should contain(participantList.head);
           _ <- cleanUpRoom(playersNumber)) yield assertion
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
        for (_ <- enterPublicRoom(2, participantList.head, notificationAddress);
             assertion <- exitPublicRoom(3).shouldFailWith[HTTPException];
             _ <- cleanUpRoom(2)) yield assertion
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
      apiCall(TOO_BIG_PLAYERS_NUMBER).shouldFailWith[HTTPException]
    }
  }

  /**
    * Cleans up the provided room, exiting the user with passed token
    */
  override protected def cleanUpRoom(roomID: String)(implicit userToken: String): Future[Unit] =
    for (_ <- exitRoom(roomID)(userToken)) yield ()

  /**
    * Cleans up the provided public room, exiting player with passed token
    */
  override protected def cleanUpRoom(playersNumber: Int)(implicit userToken: String): Future[Unit] =
    for (_ <- exitPublicRoom(playersNumber)(userToken)) yield ()
}
