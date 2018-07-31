package it.cwmp.services.rooms

import io.netty.handler.codec.http.HttpResponseStatus._
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod._
import io.vertx.scala.ext.web.client.{HttpResponse, WebClientOptions}
import it.cwmp.model.Room
import it.cwmp.model.Room.Converters._
import it.cwmp.services.rooms.ServerParameters._
import it.cwmp.testing.HttpMatchers
import it.cwmp.testing.rooms.RoomsWebServiceTesting
import it.cwmp.utils.Utils.httpStatusNameToCode
import it.cwmp.utils.VertxClient

import scala.concurrent.Future

/**
  * Test class for RoomsServiceVerticle
  *
  * @author Enrico Siboni
  */
class RoomsServiceVerticleTest extends RoomsWebServiceTesting with RoomApiWrapperUtils with HttpMatchers with VertxClient {

  override protected def clientOptions: WebClientOptions = WebClientOptions()
    .setDefaultHost("localhost")
    .setDefaultPort(DEFAULT_PORT)

  override protected def privateRoomCreationTests(roomName: String, playersNumber: Int): Unit = {
    val creationApi = API_CREATE_PRIVATE_ROOM_URL

    it("should succeed when the user is authenticated and input is valid") {
      for (response <- createPrivateRoomRequest(roomName, playersNumber);
           assertion <- assert(response.statusCode() == CREATED.code() && response.bodyAsString().isDefined)) yield assertion
    }

    describe("should fail") {
      it("if no room is sent with request") {
        client.post(creationApi).addAuthentication.sendFuture() shouldAnswerWith BAD_REQUEST
      }
      it("if body is malformed") {
        client.post(creationApi).addAuthentication.sendJsonFuture("Ciao") shouldAnswerWith BAD_REQUEST
      }
      it("if the roomName sent is empty") {
        createPrivateRoomRequest("", playersNumber) shouldAnswerWith BAD_REQUEST
      }
      it("if the playersNumber isn't valid") {
        createPrivateRoomRequest(roomName, 0) shouldAnswerWith BAD_REQUEST
      }
    }
  }

  override protected def privateRoomEnteringTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed when the user is authenticated, input is valid and room non full") {
      for (roomID <- createAPrivateRoomAndGetID(roomName, playersNumber);
           assertion <- enterPrivateRoomRequest(roomID, participantList.head, notificationAddress) shouldAnswerWith OK;
           _ <- cleanUpRoom(roomID)) yield assertion
    }

    describe("should fail") {
      onWrongRoomID(enterPrivateRoomRequest(_, participantList.head, notificationAddress))

      it("if address not provided") {
        client.put(API_ENTER_PRIVATE_ROOM_URL).addAuthentication.sendFuture() shouldAnswerWith BAD_REQUEST
      }
      it("if address message malformed") {
        client.put(API_ENTER_PRIVATE_ROOM_URL).addAuthentication.sendJsonFuture("Ciao") shouldAnswerWith BAD_REQUEST
      }
      it("if user is already inside a room") {
        for (roomID <- createAPrivateRoomAndGetID(roomName, playersNumber);
             _ <- enterPrivateRoomRequest(roomID, participantList.head, notificationAddress);
             assertion <- enterPrivateRoomRequest(roomID, participantList.head, notificationAddress) shouldAnswerWith BAD_REQUEST;
             _ <- cleanUpRoom(roomID)) yield assertion
      }
      it("if the room was already filled") {
        val playersNumber = 2
        for (roomID <- createAPrivateRoomAndGetID(roomName, playersNumber);
             _ <- enterPrivateRoomRequest(roomID, participantList.head, notificationAddress);
             _ <- enterPrivateRoomRequest(roomID, participantList(1), notificationAddress)(client, tokenList(1));
             assertion <- enterPrivateRoomRequest(roomID, participantList(2), notificationAddress)(client, tokenList(2)) shouldAnswerWith NOT_FOUND)
          yield assertion
      }
    }
  }

  override protected def privateRoomInfoRetrievalTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomId is correct") {
      for (roomID <- createAPrivateRoomAndGetID(roomName, playersNumber);
           response <- privateRoomInfoRequest(roomID);
           roomJson = Room(roomID, roomName, playersNumber, Seq()).toJson.encode();
           assertion <- assert(response.statusCode() == OK.code() && response.bodyAsString().get == roomJson))
        yield assertion
    }

    describe("should fail") {
      onWrongRoomID(privateRoomInfoRequest)
    }
  }

  override protected def privateRoomExitingTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomID is correct and user inside") {
      for (roomID <- createAPrivateRoomAndGetID(roomName, playersNumber);
           _ <- enterPrivateRoomRequest(roomID, participantList.head, notificationAddress);
           assertion <- exitPrivateRoomRequest(roomID) shouldAnswerWith OK) yield assertion
    }
    it("user should not be inside after it") {
      for (roomID <- createAPrivateRoomAndGetID(roomName, playersNumber);
           _ <- enterPrivateRoomRequest(roomID, participantList.head, notificationAddress);
           _ <- exitPrivateRoomRequest(roomID);
           response <- privateRoomInfoRequest(roomID);
           assertion <- assert(response.statusCode() == OK.code() && !response.bodyAsString().get.contains(participantList.head.username)))
        yield assertion
    }

    describe("should fail") {
      onWrongRoomID(exitPrivateRoomRequest)

      it("if user is not inside the room") {
        for (roomID <- createAPrivateRoomAndGetID(roomName, playersNumber);
             assertion <- exitPrivateRoomRequest(roomID) shouldAnswerWith BAD_REQUEST) yield assertion
      }
      it("if user is inside another room") {
        for (roomID <- createAPrivateRoomAndGetID(roomName, playersNumber);
             otherRoomID <- createAPrivateRoomAndGetID(roomName, playersNumber);
             _ <- enterPrivateRoomRequest(roomID, participantList.head, notificationAddress);
             assertion <- exitPrivateRoomRequest(otherRoomID) shouldAnswerWith BAD_REQUEST;
             _ <- cleanUpRoom(roomID)) yield assertion
      }
    }
  }

  override protected def publicRoomEnteringTests(playersNumber: Int): Unit = {
    describe("should succeed") {
      it("if room with such players number exists") {
        for (assertion <- enterPublicRoomRequest(playersNumber, participantList.head, notificationAddress) shouldAnswerWith OK;
             _ <- cleanUpRoom(playersNumber)) yield assertion
      }
      it("and the user should be inside it") {
        for (_ <- enterPublicRoomRequest(playersNumber, participantList.head, notificationAddress);
             response <- publicRoomInfoRequest(playersNumber);
             assertion <- assert(response.statusCode() == OK.code() && response.bodyAsString().get.contains(participantList.head.username));
             _ <- cleanUpRoom(playersNumber)) yield assertion
      }
      it("even when the room was filled in past") {
        for (_ <- enterPublicRoomRequest(2, participantList.head, notificationAddress);
             _ <- enterPublicRoomRequest(2, participantList(1), notificationAddress)(client, tokenList(1));
             _ <- enterPublicRoomRequest(2, participantList(2), notificationAddress)(client, tokenList(2));
             response <- publicRoomInfoRequest(2);
             assertion <- assert(response.statusCode() == OK.code() &&
               !response.bodyAsString().get.contains(participantList.head.username) &&
               !response.bodyAsString().get.contains(participantList(1).username) &&
               response.bodyAsString().get.contains(participantList(2).username));
             _ <- cleanUpRoom(playersNumber)(tokenList(2))) yield assertion
      }
    }

    describe("should fail") {
      onWrongPlayersNumber(enterPublicRoomRequest(_, participantList.head, notificationAddress))

      it("if address not provided") {
        client.put(API_ENTER_PUBLIC_ROOM_URL).addAuthentication.sendFuture() shouldAnswerWith BAD_REQUEST
      }
      it("if address message malformed") {
        client.put(API_ENTER_PUBLIC_ROOM_URL).addAuthentication.sendJsonFuture("Ciao") shouldAnswerWith BAD_REQUEST
      }
      it("if same user is already inside a room") {
        for (_ <- enterPublicRoomRequest(2, participantList.head, notificationAddress);
             assertion <- enterPublicRoomRequest(3, participantList.head, notificationAddress) shouldAnswerWith BAD_REQUEST;
             _ <- cleanUpRoom(playersNumber)) yield assertion
      }
    }
  }

  override protected def publicRoomListingTests(playersNumber: Int): Unit = {
    it("should be nonEmpty") {
      for (response <- listPublicRoomsRequest();
           assertion <- assert(response.statusCode() == OK.code() && response.bodyAsString().get.nonEmpty))
        yield assertion
    }
  }

  override protected def publicRoomInfoRetrievalTests(playersNumber: Int): Unit = {
    it("should succeed if provided playersNumber is correct") {
      publicRoomInfoRequest(playersNumber) shouldAnswerWith OK
    }
    it("should show entered players") {
      for (_ <- enterPublicRoomRequest(playersNumber, participantList.head, notificationAddress);
           response <- publicRoomInfoRequest(playersNumber);
           assertion <- assert(response.statusCode() == OK.code() && response.bodyAsString().get.contains(participantList.head.username));
           _ <- cleanUpRoom(playersNumber)) yield assertion
    }

    describe("should fail") {
      onWrongPlayersNumber(publicRoomInfoRequest)
    }
  }

  override protected def publicRoomExitingTests(playersNumber: Int): Unit = {
    it("should succeed if players number is correct and user is inside") {
      for (_ <- enterPublicRoomRequest(playersNumber, participantList.head, notificationAddress);
           assertion <- exitPublicRoomRequest(playersNumber) shouldAnswerWith OK) yield assertion
    }
    it("user should not be inside after it") {
      for (_ <- enterPublicRoomRequest(playersNumber, participantList.head, notificationAddress);
           _ <- exitPublicRoomRequest(playersNumber);
           response <- publicRoomInfoRequest(playersNumber);
           assertion <- assert(response.statusCode() == OK.code() && !response.bodyAsString().get.contains(participantList.head.username)))
        yield assertion
    }

    describe("should fail") {
      onWrongPlayersNumber(exitPublicRoomRequest)

      it("if user is not inside the room") {
        exitPublicRoomRequest(playersNumber) shouldAnswerWith BAD_REQUEST
      }
      it("if user is inside another room") {
        for (_ <- enterPublicRoomRequest(2, participantList.head, notificationAddress);
             assertion <- exitPublicRoomRequest(3) shouldAnswerWith BAD_REQUEST;
             _ <- cleanUpRoom(playersNumber)) yield assertion
      }
    }
  }

  describe("Room Api") {
    val roomApiInteractions = {
      Seq(
        (POST, API_CREATE_PRIVATE_ROOM_URL),
        (PUT, API_ENTER_PRIVATE_ROOM_URL),
        (GET, API_PRIVATE_ROOM_INFO_URL),
        (DELETE, API_EXIT_PRIVATE_ROOM_URL),
        (GET, API_LIST_PUBLIC_ROOMS_URL),
        (PUT, API_ENTER_PUBLIC_ROOM_URL),
        (GET, API_PUBLIC_ROOM_INFO_URL),
        (DELETE, API_EXIT_PUBLIC_ROOM_URL))
    }

    describe("should fail") {
      for (apiCall <- roomApiInteractions) {
        it(s"if token not provided, doing ${apiCall._1.toString} on ${apiCall._2}") {
          client.request(apiCall._1, apiCall._2).sendFuture() shouldAnswerWith BAD_REQUEST
        }
        it(s"if token wrong, doing ${apiCall._1.toString} on ${apiCall._2}") {
          client.request(apiCall._1, apiCall._2).addAuthentication("token").sendFuture() shouldAnswerWith UNAUTHORIZED
        }
        it(s"if token isn't valid, doing ${apiCall._1.toString} on ${apiCall._2}") {
          client.request(apiCall._1, apiCall._2)
            .addAuthentication("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68")
            .sendFuture() shouldAnswerWith UNAUTHORIZED
        }
      }
    }
  }

  override protected def onWrongRoomID(apiCall: String => Future[_]): Unit = {
    it("if roomID is empty") {
      apiCall("").mapTo[HttpResponse[Buffer]] shouldAnswerWith BAD_REQUEST
    }
    it("if provided roomID is not present") {
      apiCall("1234214").mapTo[HttpResponse[Buffer]] shouldAnswerWith NOT_FOUND
    }
  }

  override protected def onWrongPlayersNumber(apiCall: Int => Future[_]): Unit = {
    it("if players number provided less than 2") {
      apiCall(0).mapTo[HttpResponse[Buffer]] shouldAnswerWith BAD_REQUEST
    }
    it("if room with such players number doesn't exist") {
      apiCall(TOO_BIG_PLAYERS_NUMBER).mapTo[HttpResponse[Buffer]] shouldAnswerWith NOT_FOUND
    }
  }

  /**
    * Shorthand to create a room and get it's id from response
    */
  private def createAPrivateRoomAndGetID(roomName: String, playersNumber: Int) = {
    for (response <- createPrivateRoomRequest(roomName, playersNumber)) yield response.bodyAsString().get
  }

  /**
    * Cleans up the provided room, exiting the user with passed token
    */
  override protected def cleanUpRoom(roomID: String)(implicit userToken: String): Future[Unit] =
    for (_ <- exitPrivateRoomRequest(roomID)(client, userToken)) yield ()

  /**
    * Cleans up the provided public room, exiting player with passed token
    */
  override protected def cleanUpRoom(playersNumber: Int)(implicit userToken: String): Future[Unit] =
    for (_ <- exitPublicRoomRequest(playersNumber)(client, userToken)) yield ()
}
