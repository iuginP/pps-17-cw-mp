package it.cwmp.services.rooms

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod._
import io.vertx.scala.ext.web.client.{HttpResponse, WebClientOptions}
import it.cwmp.model.Room
import it.cwmp.model.Room.Converters._
import it.cwmp.services.rooms.ServerParameters._
import it.cwmp.testing.HttpMatchers
import it.cwmp.testing.rooms.RoomsWebServiceTesting
import it.cwmp.utils.VertxClient
import org.scalatest.Assertion

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
      createPrivateRoomRequest(roomName, playersNumber) flatMap (res => assert(res.statusCode() == 201 && res.bodyAsString().isDefined))
    }

    describe("should fail") {
      it("if no room is sent with request") {
        client.post(creationApi).addAuthentication.sendFuture() shouldAnswerWith 400
      }
      it("if body is malformed") {
        client.post(creationApi).addAuthentication.sendJsonFuture("Ciao") shouldAnswerWith 400
      }
      it("if the roomName sent is empty") {
        createPrivateRoomRequest("", playersNumber) shouldAnswerWith 400
      }
      it("if the playersNumber isn't valid") {
        createPrivateRoomRequest(roomName, 0) shouldAnswerWith 400
      }
    }
  }

  override protected def privateRoomEnteringTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed when the user is authenticated, input is valid and room non full") {
      createAPrivateRoomAndGetID(roomName, playersNumber)
        .flatMap(roomID => (enterPrivateRoomRequest(roomID, participantList.head, notificationAddress) shouldAnswerWith 200)
          .flatMap(cleanUpRoomRequest(roomID, _)))
    }

    describe("should fail") {
      onWrongRoomID(enterPrivateRoomRequest(_, participantList.head, notificationAddress))

      it("if address not provided") {
        client.put(API_ENTER_PRIVATE_ROOM_URL).addAuthentication.sendFuture() shouldAnswerWith 400
      }
      it("if address message malformed") {
        client.put(API_ENTER_PRIVATE_ROOM_URL).addAuthentication.sendJsonFuture("Ciao") shouldAnswerWith 400
      }
      it("if user is already inside a room") {
        createAPrivateRoomAndGetID(roomName, playersNumber)
          .flatMap(roomID => enterPrivateRoomRequest(roomID, participantList.head, notificationAddress)
            .flatMap(_ => enterPrivateRoomRequest(roomID, participantList.head, notificationAddress) shouldAnswerWith 400)
            .flatMap(cleanUpRoomRequest(roomID, _)))
      }
      it("if the room was already filled") {
        val playersNumber = 2
        createAPrivateRoomAndGetID(roomName, playersNumber)
          .flatMap(roomID => enterPrivateRoomRequest(roomID, participantList.head, notificationAddress)
            .flatMap(_ => enterPrivateRoomRequest(roomID, participantList(1), notificationAddress)(client, tokenList(1)))
            .flatMap(_ => enterPrivateRoomRequest(roomID, participantList(2), notificationAddress)(client, tokenList(2)))) shouldAnswerWith 404
      }
    }
  }

  override protected def privateRoomInfoRetrievalTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomId is correct") {
      var roomJson = ""
      createAPrivateRoomAndGetID(roomName, playersNumber)
        .flatMap(roomID => privateRoomInfoRequest(roomID)
          .map(response => {
            roomJson = Room(roomID, roomName, playersNumber, Seq()).toJson.encode()
            response
          }))
        .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get == roomJson))
    }

    describe("should fail") {
      onWrongRoomID(privateRoomInfoRequest)
    }
  }

  override protected def privateRoomExitingTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomID is correct and user inside") {
      createAPrivateRoomAndGetID(roomName, playersNumber)
        .flatMap(roomID => enterPrivateRoomRequest(roomID, participantList.head, notificationAddress)
          .flatMap(_ => exitPrivateRoomRequest(roomID))) shouldAnswerWith 200
    }
    it("user should not be inside after it") {
      createAPrivateRoomAndGetID(roomName, playersNumber)
        .flatMap(roomID => enterPrivateRoomRequest(roomID, participantList.head, notificationAddress)
          .flatMap(_ => exitPrivateRoomRequest(roomID))
          .flatMap(_ => privateRoomInfoRequest(roomID)))
        .flatMap(res => assert(res.statusCode() == 200 && !res.bodyAsString().get.contains(participantList.head.username)))
    }

    describe("should fail") {
      onWrongRoomID(exitPrivateRoomRequest)

      it("if user is not inside the room") {
        createAPrivateRoomAndGetID(roomName, playersNumber) flatMap (roomID => exitPrivateRoomRequest(roomID)) shouldAnswerWith 400
      }
      it("if user is inside another room") {
        createAPrivateRoomAndGetID(roomName, playersNumber)
          .flatMap(roomID => createAPrivateRoomAndGetID(roomName, playersNumber)
            .flatMap(otherRoomID => enterPrivateRoomRequest(roomID, participantList.head, notificationAddress)
              .flatMap(_ => exitPrivateRoomRequest(otherRoomID) shouldAnswerWith 400))
            .flatMap(cleanUpRoomRequest(roomID, _)))
      }
    }
  }

  override protected def publicRoomEnteringTests(playersNumber: Int): Unit = {
    describe("should succeed") {
      it("if room with such players number exists") {
        enterPublicRoomRequest(playersNumber, participantList.head, notificationAddress) shouldAnswerWith 200 flatMap (cleanUpRoomRequest(playersNumber, _))
      }
      it("and the user should be inside it") {
        (enterPublicRoomRequest(playersNumber, participantList.head, notificationAddress) flatMap (_ => publicRoomInfoRequest(playersNumber)))
          .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get.contains(participantList.head.username)))
          .flatMap(cleanUpRoomRequest(playersNumber, _))
      }
      it("even when the room was filled in past") {
        enterPublicRoomRequest(2, participantList.head, notificationAddress)
          .flatMap(_ => enterPublicRoomRequest(2, participantList(1), notificationAddress)(client, tokenList(1)))
          .flatMap(_ => enterPublicRoomRequest(2, participantList(2), notificationAddress)(client, tokenList(2)))
          .flatMap(_ => publicRoomInfoRequest(2))
          .flatMap(res => assert(res.statusCode() == 200 &&
            !res.bodyAsString().get.contains(participantList.head.username) &&
            !res.bodyAsString().get.contains(participantList(1).username) &&
            res.bodyAsString().get.contains(participantList(2).username)))
          .flatMap(cleanUpRoomRequest(playersNumber, _)(tokenList(2)))
      }
    }

    describe("should fail") {
      onWrongPlayersNumber(enterPublicRoomRequest(_, participantList.head, notificationAddress))

      it("if address not provided") {
        client.put(API_ENTER_PUBLIC_ROOM_URL).addAuthentication.sendFuture() shouldAnswerWith 400
      }
      it("if address message malformed") {
        client.put(API_ENTER_PUBLIC_ROOM_URL).addAuthentication.sendJsonFuture("Ciao") shouldAnswerWith 400
      }
      it("if same user is already inside a room") {
        enterPublicRoomRequest(2, participantList.head, notificationAddress)
          .flatMap(_ => enterPublicRoomRequest(3, participantList.head, notificationAddress))
          .shouldAnswerWith(400) flatMap (cleanUpRoomRequest(playersNumber, _))
      }
    }
  }

  override protected def publicRoomListingTests(playersNumber: Int): Unit = {
    it("should be nonEmpty") {
      listPublicRoomsRequest() flatMap (res => assert(res.statusCode() == 200 && res.bodyAsString().get.nonEmpty))
    }
  }

  override protected def publicRoomInfoRetrievalTests(playersNumber: Int): Unit = {
    it("should succeed if provided playersNumber is correct") {
      publicRoomInfoRequest(playersNumber) shouldAnswerWith 200
    }
    it("should show entered players") {
      (enterPublicRoomRequest(playersNumber, participantList.head, notificationAddress) flatMap (_ => publicRoomInfoRequest(playersNumber)))
        .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get.contains(participantList.head.username)))
        .flatMap(cleanUpRoomRequest(playersNumber, _))
    }

    describe("should fail") {
      onWrongPlayersNumber(publicRoomInfoRequest)
    }
  }

  override protected def publicRoomExitingTests(playersNumber: Int): Unit = {
    it("should succeed if players number is correct and user is inside") {
      enterPublicRoomRequest(playersNumber, participantList.head, notificationAddress)
        .flatMap(_ => exitPublicRoomRequest(playersNumber)) shouldAnswerWith 200
    }
    it("user should not be inside after it") {
      enterPublicRoomRequest(playersNumber, participantList.head, notificationAddress)
        .flatMap(_ => exitPublicRoomRequest(playersNumber))
        .flatMap(_ => publicRoomInfoRequest(playersNumber))
        .flatMap(res => assert(res.statusCode() == 200 && !res.bodyAsString().get.contains(participantList.head.username)))
    }

    describe("should fail") {
      onWrongPlayersNumber(exitPublicRoomRequest)

      it("if user is not inside the room") {
        exitPublicRoomRequest(playersNumber) shouldAnswerWith 400
      }
      it("if user is inside another room") {
        (enterPublicRoomRequest(2, participantList.head, notificationAddress) flatMap (_ => exitPublicRoomRequest(3)))
          .shouldAnswerWith(400) flatMap (cleanUpRoomRequest(playersNumber, _))
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
          client.request(apiCall._1, apiCall._2).sendFuture() shouldAnswerWith 400
        }
        it(s"if token wrong, doing ${apiCall._1.toString} on ${apiCall._2}") {
          client.request(apiCall._1, apiCall._2).addAuthentication("token").sendFuture() shouldAnswerWith 401
        }
        it(s"if token isn't valid, doing ${apiCall._1.toString} on ${apiCall._2}") {
          client.request(apiCall._1, apiCall._2)
            .addAuthentication("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68")
            .sendFuture() shouldAnswerWith 401
        }
      }
    }
  }

  override protected def onWrongRoomID(apiCall: String => Future[_]): Unit = {
    it("if roomID is empty") {
      apiCall("").mapTo[HttpResponse[Buffer]] shouldAnswerWith 400
    }
    it("if provided roomID is not present") {
      apiCall("1234214").mapTo[HttpResponse[Buffer]] shouldAnswerWith 404
    }
  }

  override protected def onWrongPlayersNumber(apiCall: Int => Future[_]): Unit = {
    it("if players number provided less than 2") {
      apiCall(0).mapTo[HttpResponse[Buffer]] shouldAnswerWith 400
    }
    it("if room with such players number doesn't exist") {
      apiCall(TOO_BIG_PLAYERS_NUMBER).mapTo[HttpResponse[Buffer]] shouldAnswerWith 404
    }
  }

  /**
    * Shorthand to create a room and get it's id from response
    */
  private def createAPrivateRoomAndGetID(roomName: String, playersNumber: Int) = {
    createPrivateRoomRequest(roomName, playersNumber) map (_.bodyAsString().get)
  }

  /**
    * Cleans up the provided room, exiting the user with passed token
    */
  private def cleanUpRoomRequest(roomID: String, assertion: Assertion)(implicit userToken: String) =
    exitPrivateRoomRequest(roomID)(client, userToken) map (_ => assertion)

  /**
    * Cleans up the provided public room, exiting player with passed token
    */
  private def cleanUpRoomRequest(playersNumber: Int, assertion: Assertion)(implicit userToken: String) =
    exitPublicRoomRequest(playersNumber)(client, userToken) map (_ => assertion)
}
