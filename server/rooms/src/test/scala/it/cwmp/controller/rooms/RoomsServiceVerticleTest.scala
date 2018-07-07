package it.cwmp.controller.rooms

import io.vertx.core.http.HttpMethod._
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.model.Room
import it.cwmp.testing.HttpMatchers
import it.cwmp.testing.server.rooms.RoomsServiceWebTesting

/**
  * Test class for RoomsServiceVerticle
  *
  * @author Enrico Siboni
  */
class RoomsServiceVerticleTest extends RoomsServiceWebTesting with HttpMatchers {

  import RoomsApiWrapper._

  private implicit val webClient: WebClient =
    WebClient.create(vertx,
      WebClientOptions()
        .setDefaultHost("localhost")
        .setDefaultPort(DEFAULT_PORT))

  private val roomName = "Stanza"
  private val playersNumber = 2

  describe("Private Room") {
    describe("Creation") {
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

    describe("Entering") {
      val enterApi = API_ENTER_PRIVATE_ROOM_URL

      it("should succeed when the user is authenticated, input is valid and room non full") {
        createAPrivateRoomAndGetID() flatMap enterPrivateRoom httpStatusCodeEquals 200
      }

      describe("should fail") {
        it("if roomId is empty") {
          enterPrivateRoom("") httpStatusCodeEquals 400
        }
        it("if provided roomId is not present") {
          enterPrivateRoom("121212") httpStatusCodeEquals 404
        }
        it("if address not provided") {
          createClientRequestWithToken(PUT, enterApi) flatMap (_.sendFuture()) httpStatusCodeEquals 400
        }
        it("if address message malformed") {
          createClientRequestWithToken(PUT, enterApi) flatMap (_.sendJsonFuture("Ciao")) httpStatusCodeEquals 400
        }
        it("if user is already inside a room") {
          createAPrivateRoomAndGetID()
            .flatMap(roomID => enterPrivateRoom(roomID)
              .flatMap(_ => enterPrivateRoom(roomID))) httpStatusCodeEquals 400
        }
        it("if the room is full") {
          val playersNumber = 2
          createPrivateRoom(roomName, playersNumber)
            .map(_.bodyAsString().get)
            .flatMap(roomID => enterPrivateRoom(roomID)
              .flatMap(_ => enterPrivateRoom(roomID))
              .flatMap(_ => enterPrivateRoom(roomID))) httpStatusCodeEquals 400
        }
      }
    }

    describe("Retrieving Info") {
      it("should succeed if roomId is correct") {
        var roomJson = ""
        createAPrivateRoomAndGetID()
          .flatMap(roomID => privateRoomInfo(roomID)
            .map(response => {
              import Room.Converters._
              roomJson = Room(roomID, roomName, playersNumber, Seq()).toJson.encode()
              response
            }))
          .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get == roomJson))
      }

      describe("should fail") {
        it("if room id is empty") {
          privateRoomInfo("") httpStatusCodeEquals 400
        }
        it("if room is not present") {
          privateRoomInfo("111515") httpStatusCodeEquals 404
        }
      }
    }

    describe("Exit Room") {
      it("should succeed if roomID is correct and user inside") {
        createAPrivateRoomAndGetID()
          .flatMap(roomID => enterPrivateRoom(roomID)
            .flatMap(_ => exitPrivateRoom(roomID))) httpStatusCodeEquals 200
      }
      it("user should not be inside after it") {
        createAPrivateRoomAndGetID()
          .flatMap(roomID => enterPrivateRoom(roomID)
            .flatMap(_ => exitPrivateRoom(roomID))
            .flatMap(_ => privateRoomInfo(roomID)))
          .flatMap(res => assert(res.statusCode() == 200 && !res.bodyAsString().get.contains(myFirstAuthorizedUser.username)))
      }

      describe("should fail") {
        it("if roomId is empty") {
          exitPrivateRoom("") httpStatusCodeEquals 400
        }
        it("if provided roomId is not present") {
          exitPrivateRoom("21312132") httpStatusCodeEquals 404
        }
        it("if user is not inside the room") {
          createAPrivateRoomAndGetID() flatMap (roomID => exitPrivateRoom(roomID)) httpStatusCodeEquals 400
        }
        it("if user is inside another room") {
          createAPrivateRoomAndGetID()
            .flatMap(roomID => createAPrivateRoomAndGetID()
              .flatMap(otherRoomID => enterPrivateRoom(otherRoomID))
              .flatMap(_ => exitPrivateRoom(roomID))) httpStatusCodeEquals 400
        }
      }
    }
  }

  describe("Public Room") {

    it("Listing should be nonEmpty") {
      listPublicRooms() flatMap (res => assert(res.statusCode() == 200 && res.bodyAsString().get.nonEmpty))
    }

    describe("Entering") {
      val enterPublicRoomApi = API_ENTER_PUBLIC_ROOM_URL

      describe("should succeed") {
        it("if room with such players number exists") {
          enterPublicRoom(playersNumber) httpStatusCodeEquals 200
        }
        it("and the user should be inside it") {
          (enterPublicRoom(playersNumber) flatMap (_ => publicRoomInfo(playersNumber)))
            .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get.contains(myFirstAuthorizedUser.username)))
        }
      }

      describe("should fail") {
        it("if provided players number is less than 2") {
          enterPublicRoom(0) httpStatusCodeEquals 400
        }
        it("if there's no room with such players number") {
          enterPublicRoom(20) httpStatusCodeEquals 404
        }
        it("if address not provided") {
          createClientRequestWithToken(PUT, enterPublicRoomApi) flatMap (_.sendFuture()) httpStatusCodeEquals 400
        }
        it("if address message malformed") {
          createClientRequestWithToken(PUT, enterPublicRoomApi) flatMap (_.sendJsonFuture("Ciao")) httpStatusCodeEquals 400
        }
        it("if same user is already inside a room") {
          (enterPublicRoom(2) flatMap (_ => enterPublicRoom(3))) httpStatusCodeEquals 400
        }
        it("if room is full") {
          enterPublicRoom(2)
            .flatMap(_ => enterPublicRoom(2))
            .flatMap(_ => enterPublicRoom(2)) httpStatusCodeEquals 400
        }
      }
    }

    describe("Retrieving Info") {
      it("should succeed if provided playersNumber is correct") {
        publicRoomInfo(playersNumber) httpStatusCodeEquals 200
      }
      it("should show entered players") {
        (enterPublicRoom(playersNumber) flatMap (_ => publicRoomInfo(playersNumber)))
          .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get.contains(myFirstAuthorizedUser.username)))
      }

      describe("should fail") {
        it("if playersNumber provided is less than 2") {
          publicRoomInfo(0) httpStatusCodeEquals 400
        }
        it("if room with such playersNumber doesn't exist") {
          publicRoomInfo(20) httpStatusCodeEquals 404
        }
      }
    }

    describe("Exiting") {
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
        it("if playersNumber provided is less than 2") {
          exitPublicRoom(0) httpStatusCodeEquals 400
        }
        it("if room with such playersNumber doesn't exist") {
          exitPublicRoom(20) httpStatusCodeEquals 404
        }
        it("if user is not inside the room") {
          exitPublicRoom(playersNumber) httpStatusCodeEquals 400
        }
        it("if user is inside another room") {
          enterPublicRoom(2) flatMap (_ => exitPublicRoom(3)) httpStatusCodeEquals 400
        }
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
    * Shorthand to create a room and get it's id from response
    */
  private def createAPrivateRoomAndGetID() = {
    createPrivateRoom(roomName, playersNumber) map (_.bodyAsString().get)
  }
}
