package it.cwmp.controller.rooms

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod._
import io.vertx.scala.ext.web.client.{HttpResponse, WebClient, WebClientOptions}
import it.cwmp.model.Room
import it.cwmp.testing.HttpMatchers
import it.cwmp.testing.server.rooms.RoomsWebServiceTesting

import scala.concurrent.Future

/**
  * Test class for RoomsServiceVerticle
  *
  * @author Enrico Siboni
  */
class RoomsServiceVerticleTest extends RoomsWebServiceTesting with HttpMatchers {

  import RoomsApiWrapper._

  private implicit val webClient: WebClient =
    WebClient.create(vertx,
      WebClientOptions()
        .setDefaultHost("localhost")
        .setDefaultPort(DEFAULT_PORT))

  override protected def privateRoomCreationTests(roomName: String, playersNumber: Int): Unit = {
    val creationApi = API_CREATE_PRIVATE_ROOM_URL

    it("should succeed when the user is authenticated and input is valid") {
      createPrivateRoom(roomName, playersNumber) flatMap (res => assert(res.statusCode() == 201 && res.bodyAsString().isDefined))
    }

    describe("should fail") {
      it("if no room is sent with request") {
        createClientRequestWithToken(POST, creationApi) flatMap (_.sendFuture()) httpStatusCodeEquals 400
      }
      it("if body is malformed") {
        createClientRequestWithToken(POST, creationApi) flatMap (_.sendJsonFuture("Ciao")) httpStatusCodeEquals 400
      }
      it("if the roomName sent is empty") {
        createPrivateRoom("", playersNumber) httpStatusCodeEquals 400
      }
      it("if the playersNumber isn't valid") {
        createPrivateRoom(roomName, 0) httpStatusCodeEquals 400
      }
    }
  }

  override protected def privateRoomEnteringTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed when the user is authenticated, input is valid and room non full") {
      createAPrivateRoomAndGetID(roomName, playersNumber) flatMap enterPrivateRoom httpStatusCodeEquals 200
    }

    describe("should fail") {
      onWrongRoomID(enterPrivateRoom)

      it("if address not provided") {
        createClientRequestWithToken(PUT, API_ENTER_PRIVATE_ROOM_URL) flatMap (_.sendFuture()) httpStatusCodeEquals 400
      }
      it("if address message malformed") {
        createClientRequestWithToken(PUT, API_ENTER_PRIVATE_ROOM_URL) flatMap (_.sendJsonFuture("Ciao")) httpStatusCodeEquals 400
      }
      it("if user is already inside a room") {
        createAPrivateRoomAndGetID(roomName, playersNumber)
          .flatMap(roomID => enterPrivateRoom(roomID)
            .flatMap(_ => enterPrivateRoom(roomID))) httpStatusCodeEquals 400
      }
      it("if the room was already filled") {
        val playersNumber = 2
        createAPrivateRoomAndGetID(roomName, playersNumber)
          .flatMap(roomID => enterPrivateRoom(roomID)
            .flatMap(_ => enterPrivateRoom(roomID)(webClient, mySecondAuthorizedUser, mySecondCorrectToken))
            .flatMap(_ => enterPrivateRoom(roomID)(webClient, myThirdAuthorizedUser, myThirdCorrectToken))) httpStatusCodeEquals 404
      }
    }
  }

  override protected def privateRoomInfoRetrievalTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomId is correct") {
      var roomJson = ""
      createAPrivateRoomAndGetID(roomName, playersNumber)
        .flatMap(roomID => privateRoomInfo(roomID)
          .map(response => {
            import Room.Converters._
            roomJson = Room(roomID, roomName, playersNumber, Seq()).toJson.encode()
            response
          }))
        .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get == roomJson))
    }

    describe("should fail") {
      onWrongRoomID(privateRoomInfo)
    }
  }

  override protected def privateRoomExitingTests(roomName: String, playersNumber: Int): Unit = {
    it("should succeed if roomID is correct and user inside") {
      createAPrivateRoomAndGetID(roomName, playersNumber)
        .flatMap(roomID => enterPrivateRoom(roomID)
          .flatMap(_ => exitPrivateRoom(roomID))) httpStatusCodeEquals 200
    }
    it("user should not be inside after it") {
      createAPrivateRoomAndGetID(roomName, playersNumber)
        .flatMap(roomID => enterPrivateRoom(roomID)
          .flatMap(_ => exitPrivateRoom(roomID))
          .flatMap(_ => privateRoomInfo(roomID)))
        .flatMap(res => assert(res.statusCode() == 200 && !res.bodyAsString().get.contains(myFirstAuthorizedUser.username)))
    }

    describe("should fail") {
      onWrongRoomID(exitPrivateRoom)

      it("if user is not inside the room") {
        createAPrivateRoomAndGetID(roomName, playersNumber) flatMap (roomID => exitPrivateRoom(roomID)) httpStatusCodeEquals 400
      }
      it("if user is inside another room") {
        createAPrivateRoomAndGetID(roomName, playersNumber)
          .flatMap(roomID => createAPrivateRoomAndGetID(roomName, playersNumber)
            .flatMap(otherRoomID => enterPrivateRoom(otherRoomID))
            .flatMap(_ => exitPrivateRoom(roomID))) httpStatusCodeEquals 400
      }
    }
  }

  override protected def publicRoomEnteringTests(playersNumber: Int): Unit = {
    describe("should succeed") {
      it("if room with such players number exists") {
        enterPublicRoom(playersNumber) httpStatusCodeEquals 200
      }
      it("and the user should be inside it") {
        (enterPublicRoom(playersNumber) flatMap (_ => publicRoomInfo(playersNumber)))
          .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get.contains(myFirstAuthorizedUser.username)))
      }
      it("even when the room was filled in past") {
        enterPublicRoom(2)
          .flatMap(_ => enterPublicRoom(2)(webClient, mySecondAuthorizedUser, mySecondCorrectToken))
          .flatMap(_ => enterPublicRoom(2)(webClient, myThirdAuthorizedUser, myThirdCorrectToken))
          .flatMap(_ => publicRoomInfo(2))
          .flatMap(res => assert(res.statusCode() == 200 &&
            !res.bodyAsString().get.contains(myFirstAuthorizedUser.username) &&
            !res.bodyAsString().get.contains(mySecondAuthorizedUser.username) &&
            res.bodyAsString().get.contains(myThirdAuthorizedUser.username)))
      }
    }

    describe("should fail") {
      onWrongPlayersNumber(enterPublicRoom)

      it("if address not provided") {
        createClientRequestWithToken(PUT, API_ENTER_PUBLIC_ROOM_URL) flatMap (_.sendFuture()) httpStatusCodeEquals 400
      }
      it("if address message malformed") {
        createClientRequestWithToken(PUT, API_ENTER_PUBLIC_ROOM_URL) flatMap (_.sendJsonFuture("Ciao")) httpStatusCodeEquals 400
      }
      it("if same user is already inside a room") {
        (enterPublicRoom(2) flatMap (_ => enterPublicRoom(3))) httpStatusCodeEquals 400
      }
    }
  }

  override protected def publicRoomListingTests(playersNumber: Int): Unit = {
    it("should be nonEmpty") {
      listPublicRooms() flatMap (res => assert(res.statusCode() == 200 && res.bodyAsString().get.nonEmpty))
    }
  }

  override protected def publicRoomInfoRetrievalTests(playersNumber: Int): Unit = {
    it("should succeed if provided playersNumber is correct") {
      publicRoomInfo(playersNumber) httpStatusCodeEquals 200
    }
    it("should show entered players") {
      (enterPublicRoom(playersNumber) flatMap (_ => publicRoomInfo(playersNumber)))
        .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get.contains(myFirstAuthorizedUser.username)))
    }

    describe("should fail") {
      onWrongPlayersNumber(publicRoomInfo)
    }
  }

  override protected def publicRoomExitingTests(playersNumber: Int): Unit = {
    it("should succeed if players number is correct and user is inside") {
      (enterPublicRoom(playersNumber) flatMap (_ => exitPublicRoom(playersNumber))) httpStatusCodeEquals 200
    }
    it("user should not be inside after it") {
      enterPublicRoom(playersNumber)
        .flatMap(_ => exitPublicRoom(playersNumber))
        .flatMap(_ => publicRoomInfo(playersNumber))
        .flatMap(res => assert(res.statusCode() == 200 && !res.bodyAsString().get.contains(myFirstAuthorizedUser.username)))
    }

    describe("should fail") {
      onWrongPlayersNumber(exitPublicRoom)

      it("if user is not inside the room") {
        exitPublicRoom(playersNumber) httpStatusCodeEquals 400
      }
      it("if user is inside another room") {
        enterPublicRoom(2) flatMap (_ => exitPublicRoom(3)) httpStatusCodeEquals 400
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
          (createClientRequest(webClient, apiCall._1, apiCall._2) flatMap (_.sendFuture())) httpStatusCodeEquals 400
        }
        it(s"if token wrong, doing ${apiCall._1.toString} on ${apiCall._2}") {
          createClientRequestWithToken(apiCall._1, apiCall._2)(webClient, "token")
            .flatMap(_.sendFuture()) httpStatusCodeEquals 401
        }
        it(s"if token isn't valid, doing ${apiCall._1.toString} on ${apiCall._2}") {
          createClientRequestWithToken(apiCall._1, apiCall._2)(webClient, "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68")
            .flatMap(_.sendFuture()) httpStatusCodeEquals 401
        }
      }
    }
  }

  /**
    * Describes the behaviour of private api when the roomID is wrong
    */
  override protected def onWrongRoomID(apiCall: String => Future[_]): Unit = {
    it("if roomID is empty") {
      apiCall("").mapTo[HttpResponse[Buffer]] httpStatusCodeEquals 400
    }
    it("if provided roomID is not present") {
      apiCall("1234214").mapTo[HttpResponse[Buffer]] httpStatusCodeEquals 404
    }
  }

  /**
    * Describes the behaviour of public api when called with wrong players number
    */
  override protected def onWrongPlayersNumber(apiCall: (Int) => Future[_]): Unit = {
    it("if players number provided less than 2") {
      apiCall(0).mapTo[HttpResponse[Buffer]] httpStatusCodeEquals 400
    }
    it("if room with such players number doesn't exist") {
      apiCall(20).mapTo[HttpResponse[Buffer]] httpStatusCodeEquals 404
    }
  }

  /**
    * Shorthand to create a room and get it's id from response
    */
  private def createAPrivateRoomAndGetID(roomName: String, playersNumber: Int) = {
    createPrivateRoom(roomName, playersNumber) map (_.bodyAsString().get)
  }
}
